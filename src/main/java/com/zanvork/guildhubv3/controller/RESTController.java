package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.Guild;
import com.zanvork.guildhubv3.model.OwnedEntityOwnershipRequest;
import com.zanvork.guildhubv3.model.Team;
import com.zanvork.guildhubv3.model.TeamInvite;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.security.core.Authentication;

/**
 *
 * @author zanvork
 */
public abstract class RESTController {
    protected User getActiveUser(Principal principal){
        User activeUser = (User) ((Authentication) principal).getPrincipal();
        return activeUser;
    }
    
    
    
    
    @Data
    protected class ChangeOwnershipRequest{
        private long userId;
    }
    
    
    @Data
    protected class OwnershipRequestRequest{
        private long requestId;
    }
    
    //Response Objects
    
    @Data
    protected class CharacterResponse{
        private long id;
        private String name;
        private String realm;
        private String className;
        private String mainSpec;
        private String offspec;
        
        CharacterResponse(WarcraftCharacter character){
            id          =   character.getId();
            name        =   character.getName();
            realm       =   character.getRealm().getKey();
            className   =   character.getCharacterClass().getClassName();
            mainSpec    =   character.getMainSpec().getSpecName();
            offspec     =   character.getOffSpec() != null ? character.getOffSpec().getSpecName() : "none";
        }
    }
    
    @Data
    protected class GuildResponse{
        private long id;
        private String name;
        private String realm;
        
        GuildResponse(Guild guild){
            id      =   guild.getId();
            name    =   guild.getName();
            realm   =   guild.getRealm().getKey();
        }
    }
    
    @Data
    protected class TeamResponse{
        private long id;
        private String name;
        private String region;
        
        TeamResponse(Team team){
            id      =   team.getId();
            name    =   team.getName();
            region  =   team.getRegion().name();
        }
    }
    
    @Data
    protected class UserResponse{
        private long id;
        private String username;
        private String email;
        private List<String> roles;
        
        UserResponse(User user){
            id          =   user.getId();
            username    =   user.getUsername();
            email       =   user.getEmailAddress();
            roles       =  user.getRoles().stream().map( (role) -> role.getName()).collect(Collectors.toList());
        }
    }
    
    
    @Data
    protected class OwnershipRequestResponse{
        private long requestId;
        private long requesterId;
        private long subjectId;
        private long ownerId;
        private String subjectType;
        
        public OwnershipRequestResponse(OwnedEntityOwnershipRequest request){
            requestId   =   request.getId();
            requesterId =   request.getRequester().getId();
            subjectId   =   request.getSubjectId();
            ownerId     =   request.getCurrentOwner().getId();
            subjectType =   request.getEntityType();
            
        }
    }
    
    @Data
    protected class TeamInviteResponse{
        private long inviteId;
        private long teamId;
        private String teamName;
        private String teamRegion;
        private long teamOwnerId;
        private long characterId;
        private String characterName;
        private String characterRealm;
        private long characterOwnerId;
        private long inviterId;
        
        TeamInviteResponse(TeamInvite invite){
            inviteId            =   invite.getId();
            teamId              =   invite.getTeam().getId();
            teamName            =   invite.getTeam().getName();
            teamRegion          =   invite.getTeam().getRegion().name();
            teamOwnerId         =   invite.getTeam().getOwner().getId();
            characterId         =   invite.getCharacter().getId();
            characterName       =   invite.getCharacter().getName();
            characterRealm      =   invite.getCharacter().getRealm().getKey();
            characterOwnerId    =   invite.getCharacter().getOwner().getId();
            inviterId           =   invite.getRequester().getId();
        }
    }
}
