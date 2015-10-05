package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.model.RestCharacter;
import com.zanvork.battlenet.model.RestCharacterItems;
import com.zanvork.battlenet.model.RestCharacterTalents;
import com.zanvork.battlenet.service.WarcraftAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zanvork.guildhubv3.model.Character;
import com.zanvork.guildhubv3.model.CharacterItem;
import com.zanvork.guildhubv3.model.CharacterSpec;
import com.zanvork.guildhubv3.model.dao.CharacterDAO;
import com.zanvork.guildhubv3.model.enums.ItemSlots;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;

/**
 *
 * @author zanvork
 */
@Service
public class CharacterService implements BackendService{
    //Battlenet Services
    @Autowired
    private WarcraftAPIService apiService;
    
    //Backend Service
    @Autowired
    private DataService dataService;

    //DAOs
    @Autowired
    private CharacterDAO characterDAO;
    
    private Map<Long, Character> characters         =   new HashMap<>();
    private Map<String, Character> charactersByName =   new HashMap<>();
    
    private final List<Character> charactersToSave  =   new ArrayList<>();
    
    private final Object 
            charactersLock          =   new Object(),
            charactersByNameLock    =   new Object();
    
    /**
     * Returns a character from the cache by id.
     * @param id id of the character 
     * @return character object requested (null if does not exist)
     */
    public Character getCharacter(long id){
        return characters.get(id);
    }
    /**
     * Loads a character from the cache based on a unique key.
     * @param key
     * @return the character associated with the key
     */
    public Character getCharacter(String key){
        return charactersByName.get(key);
    }
    /**
     * Get a character from a name, realm and region.
     * Tries to load the specified character from the cache, if it does not exist
     * then it will create the character.
     * @param name name of the character
     * @param realm realm name the character is on
     * @param region region the realm is in
     * @return character loaded or created
     */
    public Character getCharacter(String name, String realm, String region){
        String key          =   characterNameRealmRegionToKey(name, realm, region);
        Character character =   getCharacter(key);
        if (character == null || character.getId() < 1){
            character   =   createCharacter(name, realm, region);
        }
        return character;
    }
    
    /**
     * Create a new character from a name, region and realm.
     * @param name name of the character.
     * @param realm realm the character is on
     * @param region region the realm is in
     * @return the new character
     */
    public Character createCharacter(String name, String realm, String region){
        Character character =   null;
         RestCharacter restCharacter =   apiService.getCharacter(region, realm, name);
        if (restCharacter != null){
            character   =   new Character();
            character.setName(restCharacter.getName());
            character.setRealm(dataService.getRealm(DataService.realmNameRegionToKey(realm, region)));
            
            character.setCharacterClass(dataService.getCharacterClass(restCharacter.getCharClass()));
            setCharacterSpec(character, restCharacter.getTalents());
            
            character.setAverageItemLevel(restCharacter.getItems().getAverageItemLevel());
            setCharacterItems(character, restCharacter.getItems());
            
            character.setGuild(null);
            character.setOwner(null);
            charactersToSave.add(character);
        }
        return character;
    }
    
    /**
     * Sets the character's specs in the character object provided.
     * @param character character object to set the specs on
     * @param talentsData talent data to read
     */
    private void setCharacterSpec(Character character, List<RestCharacterTalents> talentsData){
        long classId    =   character.getCharacterClass().getId();
        String[] specNames  =   new String[2];
        int mainSpecId  =   0;
        specNames[0]    =   talentsData.get(0).getSpec().getName();
        if (talentsData.size() > 1){
            if (talentsData.get(1).isSelected()){
                mainSpecId  =   1;
                specNames[1]    =   talentsData.get(1).getSpec().getName();
            }
            CharacterSpec offSpec   =   dataService.getCharacterSpec(DataService.classIDSpecNameToKey(classId, specNames[1 - mainSpecId]));
            character.setOffSpec(offSpec);
        } else {
            
        }
        CharacterSpec mainSpec  =   dataService.getCharacterSpec(DataService.classIDSpecNameToKey(classId, specNames[mainSpecId]));
        character.setMainSpec(mainSpec);
    }
    
    /**
     * Set the character's item in the character object provided.
     * @param character character object to set the items in
     * @param itemsData item data to read
     */
    private void setCharacterItems(Character character, RestCharacterItems itemsData){
        List<CharacterItem> items   =   new ArrayList<>();
        itemsData.getItems().entrySet().stream().map((itemEntry) -> {
            CharacterItem item  =   new CharacterItem();
            item.setBlizzardID(itemEntry.getValue().getId());
            item.setItemLevel(itemEntry.getValue().getItemLevel());
            item.setSlot(ItemSlots.valueOf(itemEntry.getKey().toUpperCase()));
            return item;
        }).forEach((item) -> {
            items.add(item);
        });
        character.setItems(items);
    }
    
    /**
     * Takes a character and generates a unique string key.
     * @param character
     * @return a unique key
     */
    public static String characterToKey(Character character){
        return characterNameRealmRegionToKey(character.getName(), character.getRealm().getName(), character.getRealm().getRegion().name());
    }
    
    /**
     * Takes a character name, realm and region to create a unique key.
     * @param name name of the character
     * @param realm realm the character is on
     * @param region region the realm is in
     * @return a unique identifier for this character
     */
    public static String characterNameRealmRegionToKey(String name, String realm, String region){
        return name.toLowerCase() + "_" + realm.toLowerCase() + "_" + region.toLowerCase();
    }
    
    /**
     * Store all objects currently cached in service.
     */
    @Scheduled(fixedDelay=TIME_5_SECOND)
    @Override
    public void storeObjects(){
        synchronized(charactersToSave){
            characterDAO.save(charactersToSave);
            charactersToSave.clear();
        }
    }
    /**
     * Loads object from the backend database into memory.
     */
    @Scheduled(fixedDelay=TIME_5_SECOND)
    @Override
    public void updateFromBackend(){
        loadCharactersFromBackend();
    }
    
    /**
     * Load all characters from the characterDAO and store them in the characters 
     * map in the service.
     * Uses Character's id as key
     */    
    private void loadCharactersFromBackend(){
        Map<Long, Character> newCharacters          =   new HashMap<>();
        Map<String, Character> newCharactersByName  =   new HashMap<>();
        characterDAO.findAll().forEach(character -> {
            newCharacters.put(character.getId(), character);
            newCharactersByName.put(characterToKey(character), character);
        });
        synchronized (charactersLock){
            characters    =   newCharacters;
        }
        synchronized (charactersByNameLock){
            charactersByName    =   newCharactersByName;
        }
    }
    
}
