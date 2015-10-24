package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.model.RestCharacter;
import com.zanvork.battlenet.model.RestGuild;
import com.zanvork.battlenet.service.RestObjectNotFoundException;
import com.zanvork.battlenet.service.WarcraftAPIService;
import com.zanvork.guildhubv3.model.Guild;
import com.zanvork.guildhubv3.model.GuildMember;
import com.zanvork.guildhubv3.model.Role;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.dao.GuildDAO;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author zanvork
 */
@Service
public class GuildService extends OwnedEntityBackendService<Guild>{
    //Battlenet Services
    @Autowired
    private WarcraftAPIService apiService;
    
    //Backend Service
    @Autowired
    private DataService dataService;
    @Autowired
    private CharacterService characterService;
    
    
    @Autowired
    private GuildDAO dao;

    private final Logger log  =   LoggerFactory.getLogger(this.getClass());
    
    /**
     * Get guild from cache from guild name, realm and region.
     * @param name name of the guild
     * @param realm realm the guild is in
     * @param region region the realm is on
     * @return the guild matching these parameters
     */
    public Guild getGuild(String name, String realm, String region)
            throws EntityNotFoundException {
        
        String key  =   guildNameRealmRegionToKey(name, realm, region);
        Guild guild =   getEntity(key);
        return guild;
    }
    
    /**
     * Create a guild from a guild name, realm and region.
     * @param user
     * @param name name of the guild
     * @param realm realm the guild is in
     * @param region region the realm is on
     * @return the new guild created
     */
    public Guild createGuild(User user, String name, String realm, String region)
            throws RestObjectNotFoundException, EntityAlreadyExistsException {
        
        String key  =   guildNameRealmRegionToKey(name, realm, region);
        if (entityExists(key)){
            throw new EntityAlreadyExistsException(
                    "Could not create guild with key '" + key + "', a guild with that key already exists");
        }
        RestGuild guildData =   apiService.getGuild(region, realm, name);
        Guild guild   =   new Guild();
        guild.setOwner(user);
        guild.setName(guildData.getName());
        guild.setRealm(dataService.getRealm(realm, region));
        updateGuild(guild, guildData);
        return guild;
    }
    
    /**
     * Update guild from guild name, realm and region.
     * @param user
     * @param id
     * @return the updated guild
     */
    public Guild updateGuild(User user, long id)
            throws EntityNotFoundException, RestObjectNotFoundException, ReadOnlyEntityException, NotAuthorizedException{
        
        Guild guild         =   getEntity(id);
        userCanEditEntity(user, guild);    
        RestGuild guildData =   apiService.getGuild(guild.getName(), guild.getRealm().getName(), guild.getRealm().getRegionName());
        updateGuild(guild, guildData);
        return guild;
    }
    
    /**
     * Update guild members from guild name, realm and region.
     * @param user
     * @param id
     * @return The updated guild
     */
    public Guild updateGuildMembers(User user, long id)
            throws EntityNotFoundException, RestObjectNotFoundException, ReadOnlyEntityException, NotAuthorizedException{
        
        Guild guild         =   getEntity(id);
        userCanEditEntity(user, guild);
        RestGuild guildData =   apiService.getGuild(guild.getName(), guild.getRealm().getName(), guild.getRealm().getRegionName());
        updateGuild(guild, guildData, true);
        return guild;
    }
    
    /**
     * Update Guild from guild and rest guild
     * @param guild guild to update
     * @param guildData rest object containing guild data
     */
    private void updateGuild(Guild guild, RestGuild guildData){
        updateGuild(guild, guildData, false);
    }
    
    /**
     * Update Guild from guild and rest guild
     * @param guild guild to update
     * @param guildData rest object containing guild data
     * @param updateMembers boolean for whether to update the guild's members
     */
    private void updateGuild(Guild guild, RestGuild guildData, boolean updateMembers){
        Map<String, GuildMember> guildMembers   =   new HashMap<>();
        
        //dissassociate existing members of the guild
        if (updateMembers){
            guild.getMembers().stream()
                    .forEach((member) -> {
                        member.setGuild(null);
                        guildMembers.put(characterService.entityToKey(member.getMember()), member);
                    });
            guild.getMembers().clear();
        }
        //find the leader and if update members is set then populate a new list
        String region   =   guild.getRealm().getRegion().name().toLowerCase();
        guildData.getMembers().forEach(memberData -> {
            RestCharacter characterData =   memberData.getGuildMember();
            boolean isLeader    =   (memberData.getRank() == 0);
            if (isLeader || updateMembers){
                String name     =   characterData.getName();
                String realm    =   characterData.getRealm();
                WarcraftCharacter character =   characterService.getCharacter(name, realm, region);
                if (character == null){
                    character   =   characterService.createCharacter(name, realm, region);
                } 
                if (character != null){
                    String key          =   characterService.entityToKey(character);
                    GuildMember member  =   guildMembers.remove(key);
                    if (member == null){
                        member  =   new GuildMember();
                    }
                    member.setMember(character);
                    member.setRank(memberData.getRank());
                    member.setGuild(guild);
                    guild.addMember(member);
                    if (isLeader){
                        guild.setLeader(character);
                    }
                }
            }
        });
        saveEntity(guild);
    }
    
    //Overided as special case where a guild leader's owner can modify/take ownership of huild
    @Override
    protected boolean userCanEditEntity(User user, Guild guild)
            throws ReadOnlyEntityException, NotAuthorizedException{
        
        String errorText    =   "Cannot update entity with key '" + entityToKey(guild) + "'.";
        
        //Take ownership of a guild when updating it if not already owned.
        if (guild.getOwner() == null && guild.isOwnershipLocked()){
            guild.setOwner(user);
        }
        //Check the guild is not read only
        if (guild.isReadOnly()){
            throw new ReadOnlyEntityException(
                    errorText + "  It has been flagged as read only"
            );
        }
        //Check if guild is an admin
        if (!user.hasRole(Role.ROLE_ADMIN)){
            //if guild is owned by user or guild's leader is owned by user
            if (user.getId() != guild.getOwner().getId() &&
                    (guild.getLeader() == null || guild.getLeader().getOwner() == null || user.getId() != guild.getLeader().getOwner().getId())){
                throw new NotAuthorizedException(
                        errorText + "  User does has neither admin rights nor owns the object"
                );
            }
        }
        return true;
    }
    /**
     * Takes a guild and generates a unique string key.
     * @param guild
     * @return a unique key
     */
    @Override
    public String entityToKey(Guild guild){
        String key  =   "null";
        if (guild != null){
            key = guildNameRealmRegionToKey(guild.getName(), guild.getRealm().getName(), guild.getRealm().getRegion().name());
        }
        return key;
    }
    
    /**
     * Takes a guild name, realm and region to create a unique key.
     * @param name name of the guild
     * @param realm realm the guild is on
     * @param region region the realm is in
     * @return a unique identifier for this guild
     */
    public static String guildNameRealmRegionToKey(String name, String realm, String region){
        String key  =   "null";
        if (name != null && realm != null && region != null){
            key =   name.toLowerCase() + "_" + realm.toLowerCase() + "_" + region.toLowerCase();
        } 
        return key;
    }
    
    /**
     * Store all objects currently cached in service.
     */
    @Scheduled(fixedDelay=TIME_15_SECOND)
    @Override
    public void updateToBackend(){
       
    }
    /**
     * Loads object from the backend database into memory.
     */
    @Scheduled(fixedDelay=TIME_15_SECOND)
    @Override
    public void updateFromBackend(){
        super.updateFromBackend();
    }

    @Override
    protected void saveEntity(Guild entity) throws HibernateException{
        dao.save(entity);
       
        synchronized(entitiesLock){
            entities.put(entity.getId(), entity);
        }
        synchronized(entitiesByNameLock){
            entitiesByName.put(entityToKey(entity), entity);
        }
    }

    @Override
    protected void loadEntitiesFromBackend() {
        Map<Long, Guild> newEntities          =   new HashMap<>();
        Map<String, Guild> newEntitiesByName  =   new HashMap<>();
        dao.findAll().forEach(entity -> {
            newEntities.put(entity.getId(), entity);
            newEntitiesByName.put(entityToKey(entity), entity);
        });
        synchronized (entitiesLock){
            entities    =   newEntities;
        }
        synchronized (entitiesByNameLock){
            entitiesByName    =   newEntitiesByName;
        }
    }
}
