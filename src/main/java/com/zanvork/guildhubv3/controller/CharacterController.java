package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.OwnedEntityOwnershipRequest;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.services.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.WarcraftCharacterVerificationRequest;
import java.security.Principal;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/characters")
public class CharacterController  extends RESTController {
    @Autowired
    private CharacterService characterService;
    
    @RequestMapping(method = RequestMethod.POST)
    public CharacterResponse createCharacter(
            final Principal p,
            final @RequestBody NewCharacterRequest r){
        
        String  region  =   r.getRegion();
        String  realm   =   r.getRealm();
        String  name    =   r.getName();
        
        User    user    =   getActiveUser(p);
        
        WarcraftCharacter character =   characterService.createCharacter(user, name, realm, region, true);
        CharacterResponse response   =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.GET)
    public CharacterResponse getCharacter(
            final Principal p,
            final @PathVariable String region,
            final @PathVariable String realm,
            final @PathVariable String name){
        
        WarcraftCharacter   character   =   characterService.getCharacter(name, realm, region);
        CharacterResponse response   =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}", method = RequestMethod.GET)
    public CharacterResponse getCharacter(
            final Principal p,
            final @PathVariable long characterId){
        
        WarcraftCharacter   character   =   characterService.getEntity(characterId);
        CharacterResponse response      =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}", method = RequestMethod.PUT)
    public CharacterResponse updateCharacter(
            final Principal p,
            final @PathVariable long characterId){
        
        User user                   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.updateCharacter(user, characterId);
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    @RequestMapping(value = "/{characterId}/lock", method = RequestMethod.PUT)
    public CharacterResponse lockCharacter(
            final Principal p,
            final @PathVariable long characterId){
        
        User user                   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.setEntityReadOnly(user, characterId, true);
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/unlock", method = RequestMethod.PUT)
    public CharacterResponse unlockCharacter(
            final Principal p,
            final @PathVariable long characterId){
        
        User user                   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.setEntityReadOnly(user, characterId, false);
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/lock", method = RequestMethod.PUT)
    public CharacterResponse lockCharacterOwnership(
            final Principal p,
            final @PathVariable long characterId){
        
        User user                   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.setEntityOwnershipLocked(user, characterId, true);
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/unlock", method = RequestMethod.PUT)
    public CharacterResponse unlockCharacterOwnership(
            final Principal p,
            final @PathVariable long characterId){
        
        User user                   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.setEntityOwnershipLocked(user, characterId, false);
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/change", method = RequestMethod.PUT)
    public CharacterResponse changeCharacterOwnership(
            final Principal p,
            final @PathVariable long characterId,
            final @RequestBody ChangeOwnershipRequest r){
        
        User user   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.changeUser(user, characterId, r.getUserId());
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/take", method = RequestMethod.PUT)
    public CharacterResponse takeCharacterOwnership(
            final Principal p,
            final @PathVariable long characterId){
        
        User user   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.takeOwnership(user, characterId);
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/request", method = RequestMethod.POST)
    public OwnershipRequestResponse createOwnershipRequest(
            final Principal p,
            final @PathVariable long characterId){
        
        User user   =   getActiveUser(p);
        OwnedEntityOwnershipRequest request  =   characterService.requestOwnship(user, characterId);
        OwnershipRequestResponse response    =   new OwnershipRequestResponse(request);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/request", method = RequestMethod.PUT)
    public CharacterResponse approveOwnershipRequest(
            final Principal p,
            final @PathVariable long characterId,
            final @RequestBody OwnershipRequestRequest r){
        
        User user   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.approveOwnershipRequest(user, characterId, r.getRequestId());
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/request", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void rejectOwnershipRequest(
            final Principal p,
            final @PathVariable long characterId,
            final @RequestBody OwnershipRequestRequest r){
        
        User user   =   getActiveUser(p);
        characterService.rejectOwnershipRequest(user, r.getRequestId());
    } 
    
    @RequestMapping(value = "/{characterId}/ownership/verify", method = RequestMethod.POST)
    public VerificationRequestResponse createVerificationRequest(
            final Principal p,
            final @PathVariable long characterId){
        
        User user   =   getActiveUser(p);
        WarcraftCharacterVerificationRequest request    =   characterService.takeOwnershipViaVerfication(user, characterId);
        VerificationRequestResponse response    =   new VerificationRequestResponse(request);
        
        return response;
    }
    
    @RequestMapping(value = "/{characterId}/ownership/verify", method = RequestMethod.PUT)
    public CharacterResponse checkVerificationRequest(
            final Principal p,
            final @PathVariable long characterId,
            final @RequestBody VerificationCheckRequest r){
        
        User user   =   getActiveUser(p);
        WarcraftCharacter character =   characterService.checkVerificationRequest(user, characterId, r.getRequestId());
        CharacterResponse response  =   new CharacterResponse(character);
        
        return response;
    }
    
    //Request Objects
    
    @Data
    protected class NewCharacterRequest{
        private String region;
        private String realm;
        private String name;
    }
    @Data
    protected class VerificationCheckRequest{
        private long requestId;
    }
    
    
    //Response Objects
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
    
    
}
