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
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.WarcraftCharacterVerificationRequest;
import com.zanvork.guildhubv3.model.dao.WarcraftCharacterDAO;
import com.zanvork.guildhubv3.model.dao.WarcraftCharacterVerificationRequestDAO;
import com.zanvork.guildhubv3.model.enums.ItemSlots;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author zanvork
 */
@Service
public class CharacterService extends OwnedEntityBackendService<WarcraftCharacter>{
    //Battlenet Services
    @Autowired
    private WarcraftAPIService apiService;
    
    //Backend Service
    @Autowired
    private DataService dataService;
    
    @Autowired
    private WarcraftCharacterDAO dao; 
    @Autowired
    private WarcraftCharacterVerificationRequestDAO verificationRequestsDAO;
    
    private final Logger log  =   LoggerFactory.getLogger(this.getClass());
   
    private Map<Long, WarcraftCharacterVerificationRequest> verificationRequests =   new HashMap<>();
    
    private final Object verificationRequestsLock  =   new Object();
    
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
        WarcraftCharacter character =   getEntity(key);
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
        if (entityExists(key)){
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
     * @param id
     * @return the updated character
     */
    public WarcraftCharacter updateCharacter(User user, long id)
            throws EntityNotFoundException, RestObjectNotFoundException,ReadOnlyEntityException, NotAuthorizedException{
        
        WarcraftCharacter character =   getEntity(id);
        
        userCanEditEntity(user, character);
        RestCharacter characterData =   apiService.getCharacter(character.getName(), character.getRealm().getName(), character.getRealm().getRegionName());
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

        saveEntity(character);
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
    
    
    public WarcraftCharacterVerificationRequest takeOwnershipViaVerfication(User user, long characterId){
        Random random   =   new Random();
        WarcraftCharacterVerificationRequest verificationRequest   =   new WarcraftCharacterVerificationRequest();
        WarcraftCharacter character =   getEntity(characterId);
        
        try {
            canChangeEntityOwner(user, character);
        } catch (NotAuthorizedException e){
            //We don't care about not authorized exceptions, but other exceptions still matter
        }
        
        verificationRequest.setDateCreated(new Date());
        verificationRequest.setRequester(user);
        verificationRequest.setSubject(character);
        verificationRequest.setSlot(character.getItems().get(random.nextInt(character.getItems().size())).getSlot());
        saveVerificationRequest(verificationRequest);
        
        return verificationRequest;
    }
    
    public WarcraftCharacter checkVerificationRequest(User user, long characterId, long verificationRequestId){
        WarcraftCharacterVerificationRequest verificationRequest    =   getVerificationRequest(verificationRequestId);
        WarcraftCharacter character =   verificationRequest.getSubject();
        if (character.getId() != characterId){
            throw new UnexpectedEntityException(
                    "Character id expected ('" + characterId + 
                    "') does not match the id in the verification request ('" + 
                    character.getId() + "')."
            );
        }
        if (user.getId() != verificationRequest.getRequester().getId()){
            throw new NotAuthorizedException(
                    "Requesting user ('" + user.getId() + 
                    "') does not match the id in the verification request ('" + 
                    verificationRequest.getRequester().getId() + "')."
            );
            
        }
        RestCharacter characterData =   apiService.getCharacter(character.getRealm().getRegionName(), character.getRealm().getName(), character.getName());
        Map<String, RestItem> items =   characterData.getItems().getItems();
        if (items.containsKey(verificationRequest.getSlot().name().toLowerCase())){
            throw new NotAuthorizedException(
                    "Verification failed, character with id '" + characterId + 
                    "' could not be verified, itemSlot '" +
                    verificationRequest.getSlot().name().toLowerCase() + "' still equipped."
            );
        }
        character.setOwner(user);
        saveEntity(character);
        deleteVerificationRequest(verificationRequest);
        
        return character;
        
    }
    
    /**
     * Takes a character and generates a unique string key.
     * @param character
     * @return a unique key
     */
    @Override
    public String entityToKey(WarcraftCharacter character){
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
    
    
    public WarcraftCharacterVerificationRequest getVerificationRequest(long id)
            throws EntityNotFoundException{
        
        WarcraftCharacterVerificationRequest verificationRequest;
        synchronized(verificationRequestsLock){
            verificationRequest = verificationRequests.get(id);
        }
        if (verificationRequest == null){
            throw new EntityNotFoundException(
                    "Could not load WarcraftCharacterVerificationRequest with id '" + id + "'."
            );
        }
        return verificationRequest;
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
        super.updateFromBackend();
        loadCharacterVerificationRequestsFromBackend();
    }

    
    public void saveVerificationRequest(WarcraftCharacterVerificationRequest verificationRequest){
        verificationRequestsDAO.save(verificationRequest);
       
        synchronized(verificationRequestsLock){
            verificationRequests.put(verificationRequest.getId(), verificationRequest);
        }
    }
    
    public void deleteVerificationRequest(WarcraftCharacterVerificationRequest verificationRequest){
        verificationRequestsDAO.delete(verificationRequest);
       
        synchronized(verificationRequestsLock){
            verificationRequests.remove(verificationRequest.getId());
        }
    }
    
    @Override
    protected void saveEntity(WarcraftCharacter entity) throws HibernateException{
        dao.save(entity);
       
        synchronized(entitiesLock){
            entities.put(entity.getId(), entity);
        }
        synchronized(entitiesByNameLock){
            entitiesByName.put(entityToKey(entity), entity);
        }
    }

    @Override
    protected void loadEntitiesFromBackend() {
        Map<Long, WarcraftCharacter> newEntities          =   new HashMap<>();
        Map<String, WarcraftCharacter> newEntitiesByName  =   new HashMap<>();
        dao.findAll().forEach(entity -> {
            newEntities.put(entity.getId(), entity);
            newEntitiesByName.put(entityToKey(entity), entity);
        });
        synchronized (entitiesLock){
            entities    =   newEntities;
        }
        synchronized (entitiesByNameLock){
            entitiesByName    =   newEntitiesByName;
        }
    }
    
    protected void loadCharacterVerificationRequestsFromBackend() {
        Map<Long, WarcraftCharacterVerificationRequest> newVerificationRequesrs          =   new HashMap<>();
        verificationRequestsDAO.findAll().forEach(verificationRequest -> {
            newVerificationRequesrs.put(verificationRequest.getId(), verificationRequest);
        });
        synchronized (verificationRequestsLock){
            verificationRequests    =   newVerificationRequesrs;
        }
    }
    
    

}
