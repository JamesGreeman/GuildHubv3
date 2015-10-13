package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.services.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zanvork.guildhubv3.model.WarcraftCharacter;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/characters")
public class CharacterController {
    @Autowired
    private CharacterService characterService;
    
    @RequestMapping("/get/{regionName}/{realmName}/{name}")
    public WarcraftCharacter getCharacter(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        return characterService.getCharacter(name, realmName, regionName);
    }
    
    @RequestMapping("/add/{regionName}/{realmName}/{name}")
    public String addCharacter(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        if (characterService.createCharacter(name, realmName, regionName, true) != null){
            return "Successfully created character.";
        }
        return "Failed to create character.";
    }
    
    @RequestMapping("/update/{regionName}/{realmName}/{name}")
    public String updateCharacter(@PathVariable String regionName, @PathVariable String realmName, @PathVariable String name){
        if (characterService.updateCharacter(name, realmName, regionName) != null){
            return "Successfully updated character.";
        }
        return "Failed to update character.";
    }
    
}
