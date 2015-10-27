package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.OwnedEntityOwnershipRequest;
import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.model.TeamInvite;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.services.TeamService;
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
@RequestMapping("/teams")
public class TeamController extends RESTController {
    
    @Autowired
    private TeamService teamService;
    
    
    @RequestMapping(method = RequestMethod.POST)
    public TeamResponse createTeam(
            final Principal p,
            final @RequestBody NewTeamRequest r){
        
        String name     =   r.getName();
        String region   =   r.getRegion();
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.createTeam(user, name, region);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.GET)
    public TeamResponse getTeam(
            final Principal p,
            final @PathVariable String region,
            final @PathVariable String realm,
            final @PathVariable String name){
        
        Team   team             =   teamService.getTeam(name, region);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}", method = RequestMethod.GET)
    public TeamResponse getTeam(
            final Principal p,
            final @PathVariable long teamId){
        
        Team   team             =   teamService.getEntity(teamId);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteTeam(
            final Principal p,
            final @RequestBody DeleteTeamRequest r){
        
        long teamId     =   r.getTeamId();
        String password =   r.getPassword();
        User user       =   getActiveUser(p);
        
        teamService.removeTeam(user, teamId, password);
    }
    
    
    @RequestMapping(value = "/member", method = RequestMethod.POST)
    public TeamResponse addMember(
            final Principal p,
            final @RequestBody MemberRequest r){
        
        long teamId         =   r.getTeamId();
        long characterId    =   r.getCharacterId();
        User user           =   getActiveUser(p);
        
        Team team               =   teamService.addMember(user, teamId, characterId);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/member", method = RequestMethod.DELETE)
    public TeamResponse removeMember(
            final Principal p,
            final @RequestBody MemberRequest r){
        
        long teamId         =   r.getTeamId();
        long characterId    =   r.getCharacterId();
        User user           =   getActiveUser(p);
        
        Team team               =   teamService.removeMember(user, teamId, characterId);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/member/invite", method = RequestMethod.POST)
    public TeamInviteResponse inviteMember(
            final Principal p,
            final @RequestBody MemberRequest r){
        
        long teamId         =   r.getTeamId();
        long characterId    =   r.getCharacterId();
        User user           =   getActiveUser(p);
        
        TeamInvite invite           =   teamService.inviteMember(user, teamId, characterId);
        TeamInviteResponse response =   new TeamInviteResponse(invite);
        
        return response;
    }
    
    @RequestMapping(value = "/member/invite", method = RequestMethod.PUT)
    public void acceptInvite(
            final Principal p,
            final @RequestBody InviteRequest r){
        
        long inviteId   =   r.getInviteId();
        User user   =   getActiveUser(p);
        teamService.acceptTeamInvite(user, inviteId);
    }
    
    @RequestMapping(value = "/member/invite", method = RequestMethod.DELETE)
    public void rejectInvite(
            final Principal p,
            final @RequestBody InviteRequest r){
        
        long inviteId   =   r.getInviteId();
        User user   =   getActiveUser(p);
        teamService.rejectTeamInvite(user, inviteId);
    }
    
 
    @RequestMapping(value = "/{teamId}/lock", method = RequestMethod.PUT)
    public TeamResponse lockTeam(
            final Principal p,
            final @PathVariable long teamId){
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.setEntityReadOnly(user, teamId, true);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/unlock", method = RequestMethod.PUT)
    public TeamResponse unlockTeam(
            final Principal p,
            final @PathVariable long teamId){
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.setEntityReadOnly(user, teamId, false);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/ownership/lock", method = RequestMethod.PUT)
    public TeamResponse lockTeamOwnership(
            final Principal p,
            final @PathVariable long teamId){
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.setEntityOwnershipLocked(user, teamId, true);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/ownership/unlock", method = RequestMethod.PUT)
    public TeamResponse unlockTeamOwnership(
            final Principal p,
            final @PathVariable long teamId){
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.setEntityOwnershipLocked(user, teamId, false);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/ownership/change", method = RequestMethod.PUT)
    public TeamResponse changeTeamOwnership(
            final Principal p,
            final @PathVariable long teamId,
            final @RequestBody ChangeOwnershipRequest r){
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.changeUser(user, teamId, r.getUserId());
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/ownership/take", method = RequestMethod.PUT)
    public TeamResponse takeTeamOwnership(
            final Principal p,
            final @PathVariable long teamId){
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.takeOwnership(user, teamId);
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/ownership/request", method = RequestMethod.POST)
    public OwnershipRequestResponse createOwnershipRequest(
            final Principal p,
            final @PathVariable long teamId){
        
        User user                           =   getActiveUser(p);
        OwnedEntityOwnershipRequest request =   teamService.requestOwnship(user, teamId);
        OwnershipRequestResponse response   =   new OwnershipRequestResponse(request);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/ownership/request", method = RequestMethod.POST)
    public TeamResponse approveOwnershipRequest(
            final Principal p,
            final @PathVariable long teamId,
            final @RequestBody OwnershipRequestRequest r){
        
        User user               =   getActiveUser(p);
        Team team               =   teamService.approveOwnershipRequest(user, teamId, r.getRequestId());
        TeamResponse response   =   new TeamResponse(team);
        
        return response;
    }
    
    @RequestMapping(value = "/{teamId}/ownership/request", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void rejectOwnershipRequest(
            final Principal p,
            final @PathVariable long teamId,
            final @RequestBody OwnershipRequestRequest r){
        
        User user   =   getActiveUser(p);
        teamService.rejectOwnershipRequest(user, r.getRequestId());
    } 
    
    
    //Requests
    
    @Data
    protected class NewTeamRequest{
        private String name;
        private String region;
    }
    
    @Data
    protected class DeleteTeamRequest{
        private long teamId;
        private String password;
    }
    
    @Data
    protected class MemberRequest{
        private long teamId;
        private long characterId;
    }
    
    @Data
    protected class RemoveMemberRequest{
        private long teamId;
        private long characterId;
        private String password;
    }
    
    @Data
    protected class InviteRequest{
        private long inviteId;
    }
    
    //Responses
    
    protected class TeamInviteResponse{
        private long inviteId;
        private long teamId;
        private long teamOwnerId;
        private long characterId;
        private long characterOwnerId;
        
        TeamInviteResponse(TeamInvite invite){
            
        }
    }
    
}
