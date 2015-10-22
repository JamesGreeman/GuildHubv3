package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.Guild;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.services.GuildService;
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
@RequestMapping("/guilds")
public class GuildController extends RESTController{
    @Autowired
    private GuildService guildService;
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.GET)
    public Guild getGuild(
            @PathVariable String regionName, 
            @PathVariable String realmName, 
            @PathVariable String name){
        
        return guildService.getGuild(name, realmName, regionName);
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.POST)
    public String addGuild(
            Principal principal, 
            @PathVariable String regionName, 
            @PathVariable String realmName, 
            @PathVariable String name){
        
        User user   =   getActiveUser(principal);
        if (guildService.createGuild(user, name, realmName, regionName) != null){
            return "Successfully created guild.";
        }
        return "Failed to create guild.";
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.PUT)
    public String updateGuild(
            Principal principal,
            @PathVariable String regionName, 
            @PathVariable String realmName, 
            @PathVariable String name){
        
        User user   =   getActiveUser(principal);
        if (guildService.updateGuild(user, name, realmName, regionName) != null){
            return "Successfully updated guild.";
        }
        return "Failed to update guild.";
    }
    
    @RequestMapping(value = "/members/{regionName}/{realmName}/{name}", method = RequestMethod.PUT)
    public String updateGuildMembers(
            Principal principal,
            @PathVariable String regionName, 
            @PathVariable String realmName, 
            @PathVariable String name){
        
        User user   =   getActiveUser(principal);
        if (guildService.updateGuildMembers(user, name, realmName, regionName) != null){
            return "Successfully updated guild.";
        }
        return "Failed to update guild.";
    }
    
    @RequestMapping(value = "/requestOwnership/{regionName}/{realmName}/{name}", method = RequestMethod.PUT)
    public String requestOwnership(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name, Principal principal){
        return "Not yet implemented";
    }
    
}
