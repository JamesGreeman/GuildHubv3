package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.Guild;
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
public class GuildController {
    @Autowired
    private GuildService guildService;
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.GET)
    public Guild getGuild(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        return guildService.getGuild(name, realmName, regionName);
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.POST)
    public String addGuild(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        if (guildService.createGuild(name, realmName, regionName) != null){
            return "Successfully created guild.";
        }
        return "Failed to create guild.";
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.PUT)
    public String updateGuild(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        if (guildService.updateGuild(name, realmName, regionName) != null){
            return "Successfully updated guild.";
        }
        return "Failed to update guild.";
    }
    
    @RequestMapping(value = "/members/{regionName}/{realmName}/{name}", method = RequestMethod.PUT)
    public String updateGuildMembers(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        if (guildService.updateGuildMembers(name, realmName, regionName) != null){
            return "Successfully updated guild.";
        }
        return "Failed to update guild.";
    }
    
    @RequestMapping(value = "/requestOwnership/{regionName}/{realmName}/{name}", method = RequestMethod.PUT)
    public String requestOwnership(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name, Principal principal){
        return "Not yet implemented";
    }
    
}
