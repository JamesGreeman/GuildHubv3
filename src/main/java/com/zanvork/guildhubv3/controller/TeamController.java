package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.services.TeamService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/teams")
public class TeamController extends RESTController {
    
    @Autowired
    private TeamService teamService;
    
    @RequestMapping(value = "/{regionName}/{name}", method = RequestMethod.GET)
    public Team getTeam(
            @PathVariable String regionName, 
            @PathVariable String name){
        
        return teamService.getTeam(name, regionName);
    }
    
    @RequestMapping(value = "/{regionName}/{name}", method = RequestMethod.POST)
    public String addTeam(
            Principal principal,
            @PathVariable String regionName, 
            @PathVariable String name){
        
        User user   =   getActiveUser(principal);
        if (teamService.createTeam(user, name, regionName) != null){
            return "Successfully created team.";
        }
        return "Failed to create team.";
    }
    
    @RequestMapping(value = "/{regionName}/{name}/{password}", method = RequestMethod.DELETE)
    public String removeTeam(
            Principal principal,
            @PathVariable String regionName, 
            @PathVariable String name,
            @PathVariable String password){
        
        User user   =   getActiveUser(principal);
        teamService.removeTeam(user, name, regionName, password);
        return "Deleted team.";
    }
    @RequestMapping(value = "/member/{regionName}/{name}/{realmName}/{characterName}", method = RequestMethod.POST)
    public String addTeamMember(
            Principal principal,
            @PathVariable String regionName, 
            @PathVariable String name, 
            @PathVariable String realmName, 
            @PathVariable String characterName){
        
        User user   =   getActiveUser(principal);
        teamService.addMember(user, name, regionName, characterName, realmName);
        return "Added member.";
    }
    
    @RequestMapping(value = "/member/{regionName}/{name}/{realmName}/{characterName}", method = RequestMethod.DELETE)
    public String removeTeamMember(
            Principal principal,
            @PathVariable String regionName, 
            @PathVariable String name, 
            @PathVariable String realmName, 
            @PathVariable String characterName){
        
        User user   =   getActiveUser(principal);
        teamService.removeMember(user, name, regionName, characterName, realmName);
        return "Removed member.";
    }
    
    
}
