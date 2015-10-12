package com.zanvork.guildhubv3.services;

import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.model.TeamMember;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.dao.TeamDAO;
import com.zanvork.guildhubv3.model.enums.Regions;
import java.util.HashMap;
import java.util.Map;
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
    
    
    public Team getTeam(long id){
        synchronized(teamsLock){
            return teams.get(id);
        }
    }
    
    private Team getTeam(String key){
        synchronized(teamsByNameLock){
            return teamsByName.get(key);
        }
    }
    
    public Team getTeam(String name, String region){
        String key  =   teamNameRegionToKey(name, region);
        Team team   =   getTeam(key);
        return team;
    }
    
    public Team createTeam(String name, String region){
        Team team   =   getTeam(name, region);
        if (team == null){
            team    =   new Team();
            team.setName(name);
            team.setRegion(Regions.valueOf(region.toUpperCase()));
        }
        saveTeam(team);
        return team;
    }
    
    public void removeTeam(String name, String region){
        Team team   =   getTeam(name, region);
        if (team != null){
            removeTeam(team);
        }
    }
    
    public void addMember(String teamName, String region, String characterName, String realm){
        Team team                       =   getTeam(teamName, region);
        if (team != null){
            TeamMember teamMember           =   team.getMember(CharacterService.characterNameRealmRegionToKey(characterName, realm, region));
            WarcraftCharacter character     =   characterService.getCharacter(characterName, realm, region);
            if (character != null && teamMember == null){
                teamMember   =   new TeamMember();
                teamMember.setMember(character);
                team.addMember(teamMember);
                
                saveTeam(team);
            }
        }
    }
    
    public void removeMember(String teamName, String region, String characterName, String realm){
        Team team               =   getTeam(teamName, region);
        TeamMember teamMember   =   team.getMember(CharacterService.characterNameRealmRegionToKey(characterName, realm, region));

        if (teamMember != null){
            team.getMembers().remove(teamMember);
            saveTeam(team);
        }
        
    }
    
    public static String teamToKey(Team team){
        return teamNameRegionToKey(team.getName(), team.getRegion().name());
    }
    
    public static String teamNameRegionToKey(String name, String region){
        return name.toLowerCase() + "_" + region.toLowerCase();
    }
    
    private void saveTeam(Team team){
        try {
            teamDAO.save(team);
        } catch (Exception e){
            log.error("Failed to save team with id: " + team.getId(), e);
        }
        
        synchronized(teamsLock){
            teams.put(team.getId(), team);
        }
        synchronized(teamsByNameLock){
            teamsByName.put(teamToKey(team), team);
        }
    }
    
    private void removeTeam(Team team){
        try {
            teamDAO.delete(team);
        } catch (Exception e){
            log.error("Failed to delete team with id: " + team.getId(), e);
        }
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
    @Scheduled(fixedDelay=TIME_5_SECOND)
    @Override
    public void updateToBackend(){
    }
    /**
     * Loads object from the backend database into memory.
     */
    @Scheduled(fixedDelay=TIME_5_SECOND)
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
