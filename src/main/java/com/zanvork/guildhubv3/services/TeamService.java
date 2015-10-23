package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.service.RestObjectNotFoundException;
import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.model.TeamMember;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.dao.TeamDAO;
import com.zanvork.guildhubv3.model.enums.Regions;
import static com.zanvork.guildhubv3.services.BackendService.TIME_15_SECOND;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 *
 * @author zanvork
 */
@Service
public class TeamService extends OwnedEntityBackendService<Team>{
    //Backend services    
    @Autowired
    private CharacterService characterService;
    
    @Autowired
    private TeamDAO dao;
    
    private final Logger log  =   LoggerFactory.getLogger(this.getClass());
    
    public Team getTeam(String name, String region)
            throws EntityNotFoundException {
        
        String key  =   teamNameRegionToKey(name, region);
        Team team   =   getEntity(key);
        return team;
    }
    
    public Team createTeam(User user, String name, String region)
            throws RestObjectNotFoundException, EntityAlreadyExistsException {
        
        String key  =   teamNameRegionToKey(name, region);
        if (entityExists(key)){
            throw new EntityAlreadyExistsException(
                    "Could not create Team with key '" + key + "', a Team with that key already exists"
            );
        }
        Team team   =   new Team();
        team.setOwner(user);
        team.setName(name);
        team.setRegion(Regions.valueOf(region.toUpperCase()));
        saveEntity(team);
        return team;
    }
    
    public void removeTeam(User user, long id, String password)
            throws EntityNotFoundException, ReadOnlyEntityException, NotAuthorizedException, NotAuthenticatedException{
        
        Team team   =   getEntity(id);
        userCanEditEntity(user, team);
        if (!BCrypt.checkpw(password, user.getPasswordHash())){
            throw new NotAuthenticatedException(
                    "Was unable to authenticate user '" + user.getUsername() + "' as the passwords did not match"
            );
        }
        saveEntity(team);
    }
    
    public void addMember(User user, long teamId, long characterId)
            throws EntityNotFoundException, EntityAlreadyExistsException, ReadOnlyEntityException, NotAuthorizedException{
        
        Team team                       =   getEntity(teamId);
        userCanEditEntity(user, team);
        TeamMember teamMember           =   new TeamMember();
        WarcraftCharacter character     =   characterService.getEntity(characterId);
        if (team.hasMember(characterId)){
            throw new EntityAlreadyExistsException(
                "Cannot add character with id '" + teamId + "' to team with id '" + characterId + "'."
                    + "  This team already contains this character."
            );
        }
        teamMember.setMember(character);
        team.addMember(teamMember);
        saveEntity(team);
        
    }
    
    public void removeMember(User user, long teamId, long characterId)
            throws EntityNotFoundException,ReadOnlyEntityException, NotAuthorizedException{
        
        Team team               =   getEntity(teamId);
        userCanEditEntity(user, team);
        TeamMember teamMember   =   team.getMember(characterId);
        team.getMembers().remove(teamMember);
        saveEntity(team);
        
    }
    
    @Override
    public String entityToKey(Team team){
        String key  =   "null";
        if (team != null){
            key = teamNameRegionToKey(team.getName(), team.getRegion().name());
        }
        return key;
    }
    
    public static String teamNameRegionToKey(String name, String region){
        String key  =   "null";
        if (name != null &&  region != null){
            key =   name.toLowerCase() + "_" + region.toLowerCase();
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
        loadEntitiesFromBackend();
    }

    @Override
    protected void saveEntity(Team entity) throws HibernateException{
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
        Map<Long, Team> newEntities          =   new HashMap<>();
        Map<String, Team> newEntitiesByName  =   new HashMap<>();
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
