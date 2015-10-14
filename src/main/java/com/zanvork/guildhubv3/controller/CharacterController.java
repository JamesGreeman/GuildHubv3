package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.services.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import java.security.Principal;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/characters")
public class CharacterController {
    @Autowired
    private CharacterService characterService;
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.GET)
    public WarcraftCharacter getCharacter(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        return characterService.getCharacter(name, realmName, regionName);
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.POST)
    public String addCharacter(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        if (characterService.createCharacter(name, realmName, regionName, true) != null){
            return "Successfully created character.";
        }
        return "Failed to create character.";
    }
    
    @RequestMapping(value = "/{regionName}/{realmName}/{name}", method = RequestMethod.PUT)
    public String updateCharacter(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        if (characterService.updateCharacter(name, realmName, regionName) != null){
            return "Successfully updated character.";
        }
        return "Failed to update character.";
    }
    
}
