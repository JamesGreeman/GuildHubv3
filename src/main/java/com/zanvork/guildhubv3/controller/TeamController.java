package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/teams")
public class TeamController {
    
    @Autowired
    private TeamService teamService;
    
    @RequestMapping("/get/{regionName}/{name}")
    public Team getTeam(@PathVariable String regionName, @PathVariable String name){
        return teamService.getTeam(name, regionName);
    }
    
    @RequestMapping("/add/{regionName}/{name}")
    public String addTeam(@PathVariable String regionName, @PathVariable String name){
        if (teamService.createTeam(name, regionName) != null){
            return "Successfully created team.";
        }
        return "Failed to create team.";
    }
    
    @RequestMapping("/remove/{regionName}/{name}")
    public String removeTeam(@PathVariable String regionName, @PathVariable String name){
        teamService.removeTeam(name, regionName);
        return "Deleted team.";
    }
    @RequestMapping("/addMember/{regionName}/{name}/{realmName}/{characterName}")
    public String addTeamMember(@PathVariable String regionName, @PathVariable String name, @PathVariable String realmName, @PathVariable String characterName){
        teamService.addMember(name, regionName, characterName, realmName);
        return "Added member.";
    }
    
    @RequestMapping("/removeMember/{regionName}/{name}/{realmName}/{characterName}")
    public String removeTeamMember(@PathVariable String regionName, @PathVariable String name, @PathVariable String realmName, @PathVariable String characterName){
        teamService.removeMember(name, regionName, characterName, realmName);
        return "Removed member.";
    }
    
    
}
