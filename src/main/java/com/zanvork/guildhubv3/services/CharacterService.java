package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.model.RestCharacter;
import com.zanvork.battlenet.model.RestCharacterItems;
import com.zanvork.battlenet.model.RestCharacterTalents;
import com.zanvork.battlenet.model.RestItem;
import com.zanvork.battlenet.service.WarcraftAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.CharacterItem;
import com.zanvork.guildhubv3.model.CharacterSpec;
import com.zanvork.guildhubv3.model.dao.CharacterItemDAO;
import com.zanvork.guildhubv3.model.dao.WarcraftCharacterDAO;
import com.zanvork.guildhubv3.model.enums.ItemSlots;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private WarcraftCharacterDAO characterDAO;
    @Autowired
    private CharacterItemDAO characterItemDAO;
    
    private Map<Long, WarcraftCharacter> characters         =   new HashMap<>();
    private Map<String, WarcraftCharacter> charactersByName =   new HashMap<>();
    
    private final List<WarcraftCharacter> charactersToSave      =   new ArrayList<>();
    private final List<WarcraftCharacter> charactersToRemove    =   new ArrayList<>();
    private final List<CharacterItem> itemsToRemove             =   new ArrayList<>();
    
    private final Object 
            charactersLock          =   new Object(),
            charactersByNameLock    =   new Object();
    
    /**
     * Returns a character from the cache by id.
     * @param id id of the character 
     * @return character object requested (null if does not exist)
     */
    public WarcraftCharacter getCharacter(long id){
        return characters.get(id);
    }
    
    /**
     * Loads a character from the cache based on a unique key.
     * @param key
     * @return the character associated with the key
     */
    public WarcraftCharacter getCharacter(String key){
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
    public WarcraftCharacter getCharacter(String name, String realm, String region){
        String key                  =   characterNameRealmRegionToKey(name, realm, region);
        WarcraftCharacter character =   getCharacter(key);
        return character;
    }
    
    /**
     * Create a new character from a name, region and realm.
     * @param name name of the character.
     * @param realm realm the character is on
     * @param region region the realm is in
     * @return the new character
     */
    public WarcraftCharacter createCharacter(String name, String realm, String region){
        return createCharacter(name, realm, region, false);
    }
    /**
     * Create a new character from a name, region and realm.
     * @param name name of the character.
     * @param realm realm the character is on
     * @param region region the realm is in
     * @param updateDetails whether to load data from the rest API to update
     * @return the new character
     */
    public WarcraftCharacter createCharacter(String name, String realm, String region, boolean updateDetails){
        WarcraftCharacter character =   getCharacter(name, realm, region);
        if (character == null){
            character   =   new WarcraftCharacter();
            character.setName(name);
            character.setRealm(dataService.getRealm(DataService.realmNameRegionToKey(realm, region)));
            if (updateDetails){
                RestCharacter characterData =   apiService.getCharacter(region, realm, name);
                if (characterData != null){
                    updateCharacter(character, characterData);
                }
            }
        }
        return character;
    }
    
    /**
     * Updates a character from a name, region and realm.
     * @param name name of the character.
     * @param realm realm the character is on
     * @param region region the realm is in
     * @return the updated character
     */
    public WarcraftCharacter updateCharacter(String name, String realm, String region){
        String key                  =   characterNameRealmRegionToKey(name, realm, region);
        WarcraftCharacter character =   getCharacter(key);
        RestCharacter characterData =   apiService.getCharacter(region, realm, name);
        if (character != null && characterData != null){
            updateCharacter(character, characterData);
        }
        return character;
    }
    
    /**
     * Updates character information from a RestCharacter object.
     * @param character WarcraftCharacter to update
     * @param characterData RestCharacter to load information from
     */
    private void updateCharacter(WarcraftCharacter character, RestCharacter characterData){
        character.setName(characterData.getName());
        character.setCharacterClass(dataService.getCharacterClass(characterData.getCharClass()));
        updateCharacterSpec(character, characterData.getTalents());

        character.setAverageItemLevel(characterData.getItems().getAverageItemLevel());
        setCharacterItems(character, characterData.getItems());

        character.setOwner(null);
        addCharacterToSave(character);
    }
    
    /**
     * Sets the character's specs in the character object provided.
     * @param character character object to set the specs on
     * @param talentsData talent data to read
     */
    private void updateCharacterSpec(WarcraftCharacter character, List<RestCharacterTalents> talentsData){
        long classId    =   character.getCharacterClass().getId();
        String[] specNames  =   new String[2];
        int mainSpecId  =   0;
        specNames[0]    =   talentsData.get(0).getSpec().getName();
        if (talentsData.size() > 1 && talentsData.get(1).getSpec() != null){
            specNames[1]    =   talentsData.get(1).getSpec().getName();
            if (talentsData.get(1).isSelected()){
                mainSpecId  =   1;
            } 
            CharacterSpec offSpec   =   dataService.getCharacterSpec(classId, specNames[1 - mainSpecId]);
            character.setOffSpec(offSpec);
        } else {
            
        }
        CharacterSpec mainSpec  =   dataService.getCharacterSpec(classId, specNames[mainSpecId]);
        character.setMainSpec(mainSpec);
    }
    
    /**
     * Set the character's item in the character object provided.
     * @param character character object to set the items in
     * @param itemsData item data to read
     */
    private void setCharacterItems(WarcraftCharacter character, RestCharacterItems itemsData){
        Map<ItemSlots, CharacterItem> characterItems    =   new HashMap<>();
        List<CharacterItem> newItems                    =   new ArrayList<>();
        if (character.getItems() != null){
            character.getItems().stream().forEach((item) -> {
                characterItems.put(item.getSlot(), item);
            });
        }
        itemsData.getItems().entrySet().stream()
                .filter(entry -> 
                        entry.getValue() != null
                )
                .map(entry -> {
                    ItemSlots itemSlot  =   ItemSlots.valueOf(entry.getKey().toUpperCase());
                    RestItem itemData   =   entry.getValue();
                    CharacterItem item  =   new CharacterItem();
                    if (characterItems.containsKey(itemSlot)){
                        item    =   characterItems.remove(itemSlot);
                        item.setSlot(itemSlot);
                    }
                    item.setOwner(character);
                    item.setBlizzardID(itemData.getId());
                    item.setItemLevel(itemData.getItemLevel());
                    return item;
                })
                .forEach(item -> {
                    newItems.add(item);
                });
        character.setItems(newItems);
        synchronized(itemsToRemove){
            itemsToRemove.addAll(characterItems.values());
        }
    }
    
    /**
     * Takes a character and generates a unique string key.
     * @param character
     * @return a unique key
     */
    public static String characterToKey(WarcraftCharacter character){
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
    
    
    public void saveCharacters(Collection<WarcraftCharacter> characters){
        characters.forEach(character -> addCharacterToSave(character));
    }
    
    public void addCharacterToSave(WarcraftCharacter character){
        synchronized(charactersToSave){
            charactersToSave.add(character);
        }
        synchronized(charactersLock){
            characters.put(character.getId(), character);
        }
        synchronized(charactersByNameLock){
            charactersByName.put(characterToKey(character), character);
        }
    }
    /**
     * Store all objects currently cached in service.
     */
    @Scheduled(fixedDelay=TIME_5_SECOND)
    @Override
    public void updateToBackend(){
        synchronized(itemsToRemove){
            try{
            characterItemDAO.delete(itemsToRemove);
            } catch (Exception e){
                System.out.println("ERROR - Failed to remove item: " + e);
            }
            itemsToRemove.clear();
        }
        synchronized(charactersToRemove){
            try{
                characterDAO.delete(charactersToRemove);
            } catch (Exception e){
                System.out.println("ERROR - Failed to remove character: " + e);
            }
            charactersToRemove.clear();
        }
        synchronized(charactersToSave){
            try{
                characterDAO.save(charactersToSave);
            } catch (Exception e){
                System.out.println("ERROR - Failed to save character: " + e);
            }
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
        Map<Long, WarcraftCharacter> newCharacters          =   new HashMap<>();
        Map<String, WarcraftCharacter> newCharactersByName  =   new HashMap<>();
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
