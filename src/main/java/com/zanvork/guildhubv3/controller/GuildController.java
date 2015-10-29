package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.Guild;
import com.zanvork.guildhubv3.model.OwnedEntityOwnershipRequest;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.services.GuildService;
import java.security.Principal;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    
    
    @RequestMapping(method = RequestMethod.POST)
    public GuildResponse createGuild(
            final Principal p,
            final @RequestBody NewGuildRequest r){
        
        String  region  =   r.getRegion();
        String  realm   =   r.getRealm();
        String  name    =   r.getName();
        
        User    user    =   getActiveUser(p);
        
        Guild guild             =   guildService.createGuild(user.getId(), name, realm, region);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.GET)
    public GuildResponse getGuild(
            final @PathVariable String region,
            final @PathVariable String realm,
            final @PathVariable String name){
        
        Guild   guild           =   guildService.getGuild(name, realm, region);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}", method = RequestMethod.GET)
    public GuildResponse getGuild(
            final @PathVariable long guildId){
        
        Guild   guild           =   guildService.getEntity(guildId);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}", method = RequestMethod.PUT)
    public GuildResponse updateGuild(
            final Principal p,
            final @PathVariable long guildId){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.updateGuild(user.getId(), guildId);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    @RequestMapping(value = "/{guildId}/lock", method = RequestMethod.PUT)
    public GuildResponse lockGuild(
            final Principal p,
            final @PathVariable long guildId){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.setEntityReadOnly(user.getId(), guildId, true);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/unlock", method = RequestMethod.PUT)
    public GuildResponse unlockGuild(
            final Principal p,
            final @PathVariable long guildId){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.setEntityReadOnly(user.getId(), guildId, false);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/ownership/lock", method = RequestMethod.PUT)
    public GuildResponse lockGuildOwnership(
            final Principal p,
            final @PathVariable long guildId){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.setEntityOwnershipLocked(user.getId(), guildId, true);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/ownership/unlock", method = RequestMethod.PUT)
    public GuildResponse unlockGuildOwnership(
            final Principal p,
            final @PathVariable long guildId){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.setEntityOwnershipLocked(user.getId(), guildId, false);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/ownership/change", method = RequestMethod.PUT)
    public GuildResponse changeGuildOwnership(
            final Principal p,
            final @PathVariable long guildId,
            final @RequestBody ChangeOwnershipRequest r){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.changeUser(user.getId(), guildId, r.getUserId());
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/ownership/take", method = RequestMethod.PUT)
    public GuildResponse takeGuildOwnership(
            final Principal p,
            final @PathVariable long guildId){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.takeOwnership(user.getId(), guildId);
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/ownership/request", method = RequestMethod.POST)
    public OwnershipRequestResponse createOwnershipRequest(
            final Principal p,
            final @PathVariable long guildId){
        
        User user                           =   getActiveUser(p);
        OwnedEntityOwnershipRequest request =   guildService.requestOwnship(user.getId(), guildId);
        OwnershipRequestResponse response   =   new OwnershipRequestResponse(request);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/ownership/request", method = RequestMethod.PUT)
    public GuildResponse approveOwnershipRequest(
            final Principal p,
            final @PathVariable long guildId,
            final @RequestBody OwnershipRequestRequest r){
        
        User user               =   getActiveUser(p);
        Guild guild             =   guildService.approveOwnershipRequest(user.getId(), guildId, r.getRequestId());
        GuildResponse response  =   new GuildResponse(guild);
        
        return response;
    }
    
    @RequestMapping(value = "/{guildId}/ownership/request", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void rejectOwnershipRequest(
            final Principal p,
            final @PathVariable long guildId,
            final @RequestBody OwnershipRequestRequest r){
        
        User user   =   getActiveUser(p);
        guildService.rejectOwnershipRequest(user.getId(), r.getRequestId());
    } 
    
    @Data
    static class NewGuildRequest{
        private String name;
        private String region;
        private String realm;
    }
}
