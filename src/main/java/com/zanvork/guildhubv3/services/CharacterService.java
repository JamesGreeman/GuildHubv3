package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.model.RestCharacter;
import com.zanvork.battlenet.model.RestCharacterItems;
import com.zanvork.battlenet.model.RestCharacterTalents;
import com.zanvork.battlenet.model.RestItem;
import com.zanvork.battlenet.service.RestObjectNotFoundException;
import com.zanvork.battlenet.service.WarcraftAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import com.zanvork.guildhubv3.model.WarcraftCharacter;
import com.zanvork.guildhubv3.model.CharacterItem;
import com.zanvork.guildhubv3.model.CharacterSpec;
import com.zanvork.guildhubv3.model.Role;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.dao.WarcraftCharacterDAO;
import com.zanvork.guildhubv3.model.enums.ItemSlots;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private final Logger log  =   LoggerFactory.getLogger(this.getClass());
    
    private Map<Long, WarcraftCharacter> characters         =   new HashMap<>();
    private Map<String, WarcraftCharacter> charactersByName =   new HashMap<>();
    
    private final Object 
            charactersLock          =   new Object(),
            charactersByNameLock    =   new Object();
    
    /**
     * Returns a character from the cache by id.
     * @param id id of the character 
     * @return character object requested (null if does not exist)
     */
    private WarcraftCharacter getCharacter(long id){
        return characters.get(id);
    }
    
    /**
     * Check if a character with a specific key exists within the cache
     * @param key the key identifying the character
     * @return whether a character with this key exists
     */
    private boolean characterExists(String key){
        synchronized(charactersByNameLock){
            return charactersByName.containsKey(key);
        }
    }
    
    /**
     * Loads a character from the cache based on a unique key.
     * @param key
     * @return the character associated with the key
     */
    private WarcraftCharacter getCharacter(String key)
            throws EntityNotFoundException{
        WarcraftCharacter character;
        synchronized(charactersByNameLock){
            character = charactersByName.get(key);
        }
        if (character == null){
            throw new EntityNotFoundException(
                    "Could not load Character entity with key '" + key + "'."
            );
        }
        return character;
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
    public WarcraftCharacter getCharacter(String name, String realm, String region)
            throws EntityNotFoundException {
        
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
    public WarcraftCharacter createCharacter(String name, String realm, String region)
            throws RestObjectNotFoundException, EntityAlreadyExistsException {
        
        return createCharacter(null, name, realm, region, false);
    }
    /**
     * Create a new character from a name, region and realm.
     * @param user
     * @param name name of the character.
     * @param realm realm the character is on
     * @param region region the realm is in
     * @param updateDetails whether to load data from the rest API to update
     * @return the new character
     */
    public WarcraftCharacter createCharacter(User user, String name, String realm, String region, boolean updateDetails)
            throws RestObjectNotFoundException, EntityAlreadyExistsException {
        
        String key  =   characterNameRealmRegionToKey(name, realm, region);
        if (characterExists(key)){
            throw new EntityAlreadyExistsException(
                    "Could not create character with key '" + key + "', a character with that key already exists"
            );
        }
        
        WarcraftCharacter character =   new WarcraftCharacter();
        character.setName(name);
        character.setRealm(dataService.getRealm(realm, region));
        if (updateDetails){
            character.setOwner(user);
            RestCharacter characterData =   apiService.getCharacter(region, realm, name);
            updateCharacter(character, characterData);
        }
        
        return character;
    }
    
    /**
     * Updates a character from a name, region and realm.
     * @param user
     * @param name name of the character.
     * @param realm realm the character is on
     * @param region region the realm is in
     * @return the updated character
     */
    public WarcraftCharacter updateCharacter(User user, String name, String realm, String region)
            throws EntityNotFoundException, RestObjectNotFoundException,ReadOnlyEntityException, NotAuthorizedException{
        
        String key                  =   characterNameRealmRegionToKey(name, realm, region);
        WarcraftCharacter character =   getCharacter(key);
        
        userCanEditCharacter(user, character);
        RestCharacter characterData =   apiService.getCharacter(region, realm, name);
        updateCharacter(character, characterData);
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

        saveCharacter(character);
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
    }
    
    
    private boolean userCanChangeCharacterOwner(User user, WarcraftCharacter character)
            throws OwnershipLockedException, NotAuthorizedException{
        
         String errorText    =   "Cannot change ownership of character with name '" + 
                character.getName() + "' on realm '" + 
                character.getRealm().getRegion().name() + "-"  + character.getRealm().getName() + "'.";
        //Check the character is not read only
        if (character.isOwnershipLocked()){
            throw new OwnershipLockedException(
                    errorText + "  It has been had it's ownership locked."
            );
        }
        //Check if user is an admin
        if (!user.hasRole(Role.ROLE_ADMIN)){
            if (user.getId() != character.getOwner().getId()){
                throw new NotAuthorizedException(
                        errorText + "  User does has neither admin rights nor owns the object"
                );
            }
        }
        return true;
    }
    
    private boolean userCanEditCharacter(User user, WarcraftCharacter character)
            throws ReadOnlyEntityException, NotAuthorizedException{
        
        String errorText    =   "Cannot update character with name '" + 
                character.getName() + "' on realm '" + 
                character.getRealm().getRegion().name() + "-"  + character.getRealm().getName() + "'.";
        
        //Take ownership of a character when updating it if not already owned.
        if (character.getOwner() == null && character.isOwnershipLocked()){
            character.setOwner(user);
        }
        //Check the character is not read only
        if (character.isReadOnly()){
            throw new ReadOnlyEntityException(
                    errorText + "  It has been flagged as read only"
            );
        }
        //Check if user is an admin
        if (!user.hasRole(Role.ROLE_ADMIN)){
            if (user.getId() != character.getOwner().getId()){
                throw new NotAuthorizedException(
                        errorText + "  User does has neither admin rights nor owns the object"
                );
            }
        }
        return true;
    }
    
    /**
     * Takes a character and generates a unique string key.
     * @param character
     * @return a unique key
     */
    public static String characterToKey(WarcraftCharacter character){
        String key  =   "null";
        if (character != null){
            key = characterNameRealmRegionToKey(character.getName(), character.getRealm().getName(), character.getRealm().getRegion().name());
        }
        return key;
    }
    
    /**
     * Takes a character name, realm and region to create a unique key.
     * @param name name of the character
     * @param realm realm the character is on
     * @param region region the realm is in
     * @return a unique identifier for this character
     */
    public static String characterNameRealmRegionToKey(String name, String realm, String region){
        String key  =   "null";
        if (name != null && realm != null && region != null){
            key =   name.toLowerCase() + "_" + realm.toLowerCase() + "_" + region.toLowerCase();
        } 
        return key;
    }
    
    
    public void saveCharacters(Collection<WarcraftCharacter> characters){
        characters.forEach(character -> saveCharacter(character));
    }
    
    public void saveCharacter(WarcraftCharacter character)
            throws HibernateException{
        characterDAO.save(character);
       
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
    @Scheduled(fixedDelay=TIME_15_SECOND)
    @Override
    public void updateToBackend(){
        
    }
    /**
     * Loads object from the backend database into memory.
     */
    @Scheduled(fixedDelay=TIME_15_SECOND)
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
