package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.service.RestObjectNotFoundException;
import com.zanvork.guildhubv3.model.Role;
import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.model.TeamMember;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.dao.TeamDAO;
import com.zanvork.guildhubv3.model.enums.Regions;
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
public class TeamService implements BackendService{
    //Backend services    
    @Autowired
    private CharacterService characterService;

    //DAOs
    @Autowired
    private TeamDAO teamDAO;
    
    private final Logger log  =   LoggerFactory.getLogger(this.getClass());
    
    private Map<Long, Team>    teams        =   new HashMap<>();
    private Map<String, Team>  teamsByName  =   new HashMap<>();
    
    
    private final Object
            teamsLock       =   new Object(),
            teamsByNameLock =   new Object();
    
    
    private Team getTeam(long id){
        synchronized(teamsLock){
            return teams.get(id);
        }
    }
    
    private boolean teamExists(String key){
        synchronized(teamsByNameLock){
            return teamsByName.containsKey(key);
        }
    }
    
    private Team getTeam(String key)
            throws EntityNotFoundException{
        
        Team team;
        synchronized(teamsByNameLock){
            team = teamsByName.get(key);
        }
        if (team == null){
            throw new EntityNotFoundException(
                    "Could not load Team entity with key '" + key + "'."
            );
        }
        return team;
    }
    
    public Team getTeam(String name, String region)
            throws EntityNotFoundException {
        
        String key  =   teamNameRegionToKey(name, region);
        Team team   =   getTeam(key);
        return team;
    }
    
    public Team createTeam(User user, String name, String region)
            throws RestObjectNotFoundException, EntityAlreadyExistsException {
        
        String key  =   teamNameRegionToKey(name, region);
        if (teamExists(key)){
            throw new EntityAlreadyExistsException(
                    "Could not create Team with key '" + key + "', a Team with that key already exists"
            );
        }
        Team team   =   new Team();
        team.setOwner(user);
        team.setName(name);
        team.setRegion(Regions.valueOf(region.toUpperCase()));
        saveTeam(team);
        return team;
    }
    
    public void removeTeam(User user, String name, String region, String password)
            throws EntityNotFoundException, ReadOnlyEntityException, NotAuthorizedException, NotAuthenticatedException{
        
        Team team   =   getTeam(name, region);
        userCanEditTeam(user, team);
        if (!BCrypt.checkpw(password, user.getPasswordHash())){
            throw new NotAuthenticatedException(
                    "Was unable to authenticate user '" + user.getUsername() + "' as the passwords did not match"
            );
        }
        removeTeam(team);
    }
    
    public void addMember(User user, String teamName, String region, String characterName, String realm)
            throws EntityNotFoundException, EntityAlreadyExistsException, ReadOnlyEntityException, NotAuthorizedException{
        
        Team team                       =   getTeam(teamName, region);
        userCanEditTeam(user, team);
        TeamMember teamMember           =   new TeamMember();
        WarcraftCharacter character     =   characterService.getCharacter(characterName, realm, region);
        String characterKey             =   CharacterService.characterToKey(character);
        if (team.hasMember(characterKey)){
            throw new EntityAlreadyExistsException(
                "Cannot add character with key '" + characterKey + "' to team '" + teamName + "' in region '" + region + "'."
                    + "  This team already contains this character."
            );
        }
        teamMember.setMember(character);
        team.addMember(teamMember);
        saveTeam(team);
        
    }
    
    public void removeMember(User user, String teamName, String region, String characterName, String realm)
            throws EntityNotFoundException,ReadOnlyEntityException, NotAuthorizedException{
        
        Team team               =   getTeam(teamName, region);
        
        userCanEditTeam(user, team);
        TeamMember teamMember   =   team.getMember(CharacterService.characterNameRealmRegionToKey(characterName, realm, region));
        team.getMembers().remove(teamMember);
        saveTeam(team);
        
    }
    private boolean userCanChangeTeamOwner(User user, Team team)
            throws OwnershipLockedException, NotAuthorizedException{
        
         String errorText    =   "Cannot change ownership of team with name '" + 
                team.getName() + "' on realm '" + 
                team.getRegion().name() + "'.";
        //Check the team is not read only
        if (team.isOwnershipLocked()){
            throw new OwnershipLockedException(
                    errorText + "  It has been had it's ownership locked."
            );
        }
        //Check if user is an admin
        if (!user.hasRole(Role.ROLE_ADMIN)){
            if (user.getId() != team.getOwner().getId()){
                throw new NotAuthorizedException(
                        errorText + "  User does has neither admin rights nor owns the object"
                );
            }
        }
        return true;
    }
    
    private boolean userCanEditTeam(User user, Team team)
            throws ReadOnlyEntityException, NotAuthorizedException{
        
        String errorText    =   "Cannot update character with name '" + 
                team.getName() + "' in region '" + 
                team.getRegion().name() + "'.";
        
        //Take ownership of a team when updating it if not already owned.
        if (team.getOwner() == null && team.isOwnershipLocked()){
            team.setOwner(user);
        }
        //Check the team is not read only
        if (team.isReadOnly()){
            throw new ReadOnlyEntityException(
                    errorText + "  It has been flagged as read only"
            );
        }
        //Check if user is an admin
        if (!user.hasRole(Role.ROLE_ADMIN)){
            if (user.getId() != team.getOwner().getId()){
                throw new NotAuthorizedException(
                        errorText + "  User does has neither admin rights nor owns the object"
                );
            }
        }
        return true;
    }
    
    public static String teamToKey(Team team){
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
    
    private void saveTeam(Team team)
            throws HibernateException {
        
        teamDAO.save(team);
        
        synchronized(teamsLock){
            teams.put(team.getId(), team);
        }
        synchronized(teamsByNameLock){
            teamsByName.put(teamToKey(team), team);
        }
    }
    
    private void removeTeam(Team team)
            throws HibernateException {
        
        teamDAO.delete(team);
        
        synchronized(teamsLock){
            teams.remove(team.getId());
        }
        synchronized(teamsByNameLock){
            teamsByName.remove(teamToKey(team));
        }
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
        loadTeamsFromBackend();
    }
    
    /**
     * Load all guilds from the guildDAO and store them in the guilds map in the service.
     * Uses Guilds's id as key
     */    
    private void loadTeamsFromBackend(){
        Map<Long, Team> newTeams            =   new HashMap<>();
        Map<String, Team> newTeamsByName    =   new HashMap<>();
        teamDAO.findAll().forEach(team -> {
            newTeams.put(team.getId(), team);
            newTeamsByName.put(teamToKey(team), team);
        });
        synchronized (teamsLock){
            teams    =   newTeams;
        }
        synchronized (teamsByNameLock){
            teamsByName    =   newTeamsByName;
        }
    }
}
