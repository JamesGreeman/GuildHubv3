package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.service.RestObjectNotFoundException;
import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.model.TeamInvite;
import com.zanvork.guildhubv3.model.TeamMember;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.dao.TeamDAO;
import com.zanvork.guildhubv3.model.dao.TeamInviteDAO;
import com.zanvork.guildhubv3.model.enums.Regions;
import static com.zanvork.guildhubv3.services.BackendService.TIME_15_SECOND;
import java.util.Date;
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
    @Autowired
    private TeamInviteDAO invitesDAO; 
    
    private final Logger log  =   LoggerFactory.getLogger(this.getClass());
    
    private Map<Long, TeamInvite> teamInvites =   new HashMap<>();
    
    private final Object teamInvitesLock    =   new Object();
    
    public Team getTeam(String name, String region)
            throws EntityNotFoundException {
        
        String key  =   teamNameRegionToKey(name, region);
        Team team   =   getEntity(key);
        return team;
    }
    
    public Team createTeam(long userId, String name, String region)
            throws RestObjectNotFoundException, EntityAlreadyExistsException {
        
        User user   =   userService.getUser(userId);
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
    
    public void removeTeam(long userId, long id, String password)
            throws EntityNotFoundException, ReadOnlyEntityException, NotAuthorizedException, NotAuthenticatedException{
        
        User user   =   userService.getUser(userId);
        Team team   =   getEntity(id);
        userCanEditEntity(userId, team);
        if (!BCrypt.checkpw(password, user.getPasswordHash())){
            throw new NotAuthenticatedException(
                    "Was unable to authenticate user '" + user.getUsername() + "' as the passwords did not match"
            );
        }
        deleteEntity(team);
    }
    
    public Team addMember(long userId, long teamId, long characterId)
            throws EntityNotFoundException, EntityAlreadyExistsException, ReadOnlyEntityException, NotAuthorizedException{
        
        Team team                       =   getEntity(teamId);
        userCanEditEntity(userId, team);
        WarcraftCharacter character     =   characterService.getEntity(characterId);
        characterService.userCanEditEntity(userId, character);
        if (team.hasMember(characterId)){
            throw new EntityAlreadyExistsException(
                "Cannot add character with id '" + characterId + "' to team with id '" + teamId + "'."
                    + "  This team already contains this character."
            );
        }
        TeamMember teamMember           =   new TeamMember();
        teamMember.setMember(character);
        team.addMember(teamMember);
        saveEntity(team);
        
        return team;        
    }
    
    
    public TeamInvite inviteMember(long userId, long teamId, long characterId)
            throws EntityNotFoundException, EntityAlreadyExistsException, ReadOnlyEntityException, NotAuthorizedException{
         
        User user                       =   userService.getUser(userId);
        Team team                       =   getEntity(teamId);
        userCanEditEntity(userId, team);
        WarcraftCharacter character     =   characterService.getEntity(characterId);
        if (team.hasMember(characterId)){
            throw new EntityAlreadyExistsException(
                "Cannot invite character with id '" + characterId + "' to team with id '" + teamId + "'."
                    + "  This team already contains this character."
            );
        }
        TeamInvite invite   =   new TeamInvite();
        invite.setCharacterInvitedId(character.getId());
        invite.setDateCreated(new Date());
        invite.setTeamId(team.getId());
        invite.setRequesterId(user.getId());
        invite.setCharacterOwnerId(character.getOwner().getId());
        
        saveInvite(invite);
        
        return invite;
    }
    
    public Team removeMember(long userId, long teamId, long characterId)
            throws EntityNotFoundException,ReadOnlyEntityException, NotAuthorizedException{
        
        Team team               =   getEntity(teamId);
        userCanEditEntity(userId, team);
        TeamMember teamMember   =   team.getMember(characterId);
        team.getMembers().remove(teamMember);
        saveEntity(team);
        
        return team;
    }
    
    public void acceptTeamInvite(long userId, long inviteId){
        TeamInvite invite           =   getInvite(inviteId);
        WarcraftCharacter character =   characterService.getEntity(invite.getCharacterInvitedId());
        Team team                   =   getEntity(invite.getTeamId());
        characterService.canChangeEntityOwner(userId, character);
        long characterId            =   invite.getCharacterInvitedId();
        long teamId                 =   invite.getTeamId();
        if (team.hasMember(characterId)){
            throw new EntityAlreadyExistsException(
                "Cannot join team with id '" + teamId + "' on character with id '" + characterId + "'."
                    + "  This team already contains this character."
            );
        }
        TeamMember teamMember   =   new TeamMember();
        teamMember.setMember(character);
        teamMember.setTeam(team);
        team.addMember(teamMember);
        
        saveEntity(team);
        deleteInvite(invite);
    }
    
    public void rejectTeamInvite(long userId, long inviteId){
        TeamInvite invite           =   getInvite(inviteId);
        WarcraftCharacter character =   characterService.getEntity(invite.getCharacterInvitedId());
        characterService.canChangeEntityOwner(userId, character);
        deleteInvite(invite);
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
    
    public TeamInvite getInvite(long inviteId)
          throws EntityNotFoundException{
        
        TeamInvite invite;
        synchronized(teamInvitesLock){
            invite = teamInvites.get(inviteId);
        }
        if (invite == null){
            throw new EntityNotFoundException(
                    "Could not load TeamInvite with id '" + inviteId + "'."
            );
        }
        return invite;
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
        loadTeamInvitesFromBackend();
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
    
    protected void deleteEntity(Team entity) throws HibernateException{
        try{
            dao.delete(entity.getId());
        } catch (Exception e){
            System.out.print(e);
        }
        synchronized(entitiesLock){
            entities.remove(entity.getId());
        }
        synchronized(entitiesByNameLock){
            entitiesByName.remove(entityToKey(entity));
        }
    }

    public void saveInvite(TeamInvite invite){
        invitesDAO.save(invite);
       
        synchronized(teamInvitesLock){
            teamInvites.put(invite.getId(), invite);
        }
    }

    public void deleteInvite(TeamInvite invite){
        invitesDAO.delete(invite);
       
        synchronized(teamInvitesLock){
            teamInvites.remove(invite.getId());
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
    protected void loadTeamInvitesFromBackend() {
        Map<Long, TeamInvite> newInvites    =   new HashMap<>();
        invitesDAO.findAll().forEach(entity -> {
            newInvites.put(entity.getId(), entity);
        });
        synchronized (teamInvitesLock){
            teamInvites    =   newInvites;
        }
    }
}
