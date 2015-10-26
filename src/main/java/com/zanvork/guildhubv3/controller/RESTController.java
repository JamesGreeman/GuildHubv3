package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.OwnedEntityOwnershipRequest;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.WarcraftCharacterVerificationRequest;
import java.security.Principal;
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
    protected class VerificationCheckRequest{
        private long requestId;
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
            realm       =   character.getRealm().getRegionName() + "-" + character.getRealm().getName();
            className   =   character.getCharacterClass().getClassName();
            mainSpec    =   character.getMainSpec().getSpecName();
            offspec     =   character.getOffSpec() != null ? character.getOffSpec().getSpecName() : "none";
        }
    }
    
    @Data
    protected class VerificationRequestResponse{
        private long requestId;
        private long requesterId;
        private long subjectId;
        private String itemSlot;

        public VerificationRequestResponse(WarcraftCharacterVerificationRequest request) {
            requestId   =   request.getId();
            requesterId =   request.getRequester().getId();
            subjectId   =   request.getSubject().getId();
            itemSlot    =   request.getSlot().name();
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
    
}
