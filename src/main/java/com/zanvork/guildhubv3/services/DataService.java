package com.zanvork.guildhubv3.services;

import com.zanvork.battlenet.model.RestClass;
import com.zanvork.battlenet.model.RestRealm;
import com.zanvork.battlenet.service.WarcraftAPIService;
import com.zanvork.guildhubv3.model.CharacterClass;
import com.zanvork.guildhubv3.model.CharacterSpec;
import com.zanvork.guildhubv3.model.Realm;
import com.zanvork.guildhubv3.model.dao.CharacterClassDAO;
import com.zanvork.guildhubv3.model.dao.CharacterSpecDAO;
import com.zanvork.guildhubv3.model.dao.RealmDAO;
import com.zanvork.guildhubv3.model.enums.Regions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author zanvork
 */
@Service
public class DataService implements BackendService{
    private static final String DEFAULT_SPEC_NAME   =   "Unknown";
    
    //Battlenet Services
    @Autowired
    private WarcraftAPIService apiService;
    
    //Backend Service
    //none

    //DAOs
    @Autowired
    private RealmDAO realmDAO;
    @Autowired
    private CharacterClassDAO characterClassDAO;
    @Autowired
    private CharacterSpecDAO characterSpecDAO;
    
    private Map<String, Realm> realms                           =   new HashMap<>();
    private Map<Long, CharacterClass> characterClasses          =   new HashMap<>();
    private Map<String, CharacterSpec> characterSpecs             =   new HashMap<>();
    
    private final List<Realm> realmsToSave                      =   new ArrayList<>();
    private final List<CharacterClass> characterClassesToSave   =   new ArrayList<>();
    private final List<CharacterSpec> characterSpecsToSave      =   new ArrayList<>();
    
    private final Object 
            realmsLock          =   new Object(), 
            characterClassesLock  =   new Object(), 
            characterSpecsLock   =   new Object();
    
    /**
     * Load the realm requested from the service's cache.
     * @param key the realm key required
     * @return realm requested
     */
    public Realm getRealm(String key){
        synchronized(realmsLock){
            return realms.get(key);
        }
    }
    
    /**
     * Load the character class requested from the service's cache.
     * @param id the class id required
     * @return class requested
     */
    public CharacterClass getCharacterClass(long id){
        synchronized(characterClassesLock){
            return characterClasses.get(id);
        }
    }
    
    
    /**
     * Load the character spec requested from the service's cache.
     * @param key the spec key required
     * @return spec requested
     */
    public CharacterSpec getCharacterSpec(String key){
        synchronized(characterSpecsLock){
            return characterSpecs.get(key);
        }        
    }
    
    /**
     * Loads a character spec from a class id and spec name.
     * Will create a new spec using this information if one does not exist.
     * @param classId class id to load the spec for
     * @param specName name of the spec
     * @return the character spec requested
     */
    public CharacterSpec getCharacterSpec(long classId, String specName){
        if (specName ==  null){
            specName    =   DEFAULT_SPEC_NAME;
        }
        CharacterSpec spec  =   getCharacterSpec(classIDSpecNameToKey(classId, specName));
        if (spec == null || spec.getId() < 1){
            spec    =   createCharacterSpec(classId, specName);
        }
        return spec;
    }
    
    /**
     * Creates a new character spec using the class id and spec name provided.
     * @param classId class id for the spec
     * @param specName name of the spec
     * @return the new spec that has been created
     */
    public CharacterSpec createCharacterSpec(long classId, String specName){
        CharacterSpec spec  =   new CharacterSpec();
        spec.setCharacterClass(getCharacterClass(classId));
        spec.setSpecName(specName);
        synchronized(characterSpecsToSave){
            characterSpecsToSave.add(spec);
        }
        return spec;
    }
    /**
     * Turns a realm object into a key.
     * @param realm realm to be converted
     * @return the key
     */
    public static String realmToKey(Realm realm){
        return realmNameRegionToKey(realm.getName(),realm.getRegion().name());
    }
    
    /**
     * Takes a Realm name and Region and concatenate them into a key.
     * @param realmName name of the realm
     * @param region name of the region
     * @return lower case concatenated string.
     */
    public static String realmNameRegionToKey(String realmName, String region){
        return realmName.toLowerCase() + "_" + region.toLowerCase();
    }
    
    
    /**
     * Turns a characterSpec object into a key.
     * @param characterSpec characterSpec to be converted
     * @return the key
     */
    public static String characterSpecToKey(CharacterSpec characterSpec){
        return classIDSpecNameToKey(characterSpec.getCharacterClass().getId(),characterSpec.getSpecName());
    }
    
    /**
     * Takes a class id and spec name and concatenate them into a key.
     * @param classId id of the class
     * @param specName name of the spec
     * @return lower case concatenated string.
     */
    public static String classIDSpecNameToKey(long classId, String specName){
        if (specName == null){
            specName    =   DEFAULT_SPEC_NAME;
        }
        return String.valueOf(classId) + "_" + specName.toLowerCase();
        
    }
    
    /**
     * Loads objects from the Blizzard Rest API service.
     * Objects are loaded via a call to the Blizzard REST API and cached for 
     * storage in the backend database
     */
    @Scheduled(initialDelay=TIME_30_SECOND, fixedDelay=TIME_1_HOUR)
    public void updateFromRest(){
        importRealmsFromRest();
        importCharacterClassesFromRest();
    }
    
    /**
     * Loads realms from the battle.net rest API and stored them in backend database.
     */
    private void importRealmsFromRest(){
        for (Regions region : Regions.values()){
            List<RestRealm> restRealms  =   apiService.getRealms(region.toString());
            restRealms.forEach(restRealm -> {
                String key  =   realmNameRegionToKey(restRealm.getName(), region.name());
                boolean addRealm;
                synchronized(realmsLock){
                    addRealm    =   !realms.containsKey(key);
                }
                if (addRealm){
                    Realm realm =   new Realm();
                    realm.setName(restRealm.getName());
                    realm.setSlug(restRealm.getSlug());
                    realm.setRegion(region);
                    synchronized(realmsToSave){
                        realmsToSave.add(realm);
                    }
                }
                
            });
        }
    }
    
    /**
     * Loads character classes from the battle.net API and stores them in backend database.
     */
    private void importCharacterClassesFromRest(){
        List<RestClass> restClasses =   apiService.getClasses();
        restClasses.forEach(restClass -> {
            boolean addClass;
            synchronized(characterClassesLock){
                addClass    =   !characterClasses.containsKey(restClass.getId());
            }
            if (addClass){
                CharacterClass characterClass   =   new CharacterClass();
                characterClass.setId(restClass.getId());
                characterClass.setClassName(restClass.getName()); 
                synchronized(characterClassesToSave){
                    characterClassesToSave.add(characterClass);
                }
            }
        });
    }
    
    /**
     * Store all objects currently cached in service.
     */
    @Scheduled(fixedDelay=TIME_5_SECOND)
    @Override
    public void storeObjects(){
        synchronized(realmsToSave){
            realmDAO.save(realmsToSave);
            realmsToSave.clear();
        }
        synchronized(characterClassesToSave){
            characterClassDAO.save(characterClassesToSave);
            characterClassesToSave.clear();
        }
        synchronized(characterSpecsToSave){
            characterSpecDAO.save(characterSpecsToSave);
            characterSpecsToSave.clear();
        }
    }
    
    /**
     * Loads object from the backend database into memory.
     */
    @Scheduled(fixedDelay=TIME_5_SECOND)
    @Override
    public void updateFromBackend(){
        loadRealmsFromBackend();
        loadCharacterClassesFromBackend();
        loadCharacterSpecsFromBackend();
    }
    
    
    /**
     * Load all realms from the realmDAO and store them in the realms map in the service.
     * Uses the realm and region name as key
     */
    private void loadRealmsFromBackend(){
        Map<String, Realm> newRealms    =   new HashMap<>();
        realmDAO.findAll().forEach(realm -> newRealms.put(realmToKey(realm), realm));
        synchronized (realmsLock){
            realms  =   newRealms;
        }
    }
    /**
     * Load all character classes from the characterClassDAO and store them in 
     * the characterClasses map in the service.
     * Uses CharacterClass's id as key
     */    
    private void loadCharacterClassesFromBackend(){
        Map<Long, CharacterClass> newCharacterClasses   =   new HashMap<>();
        characterClassDAO.findAll().forEach(characterClass -> newCharacterClasses.put(characterClass.getId(), characterClass));
        synchronized (characterClassesLock){
            characterClasses    =   newCharacterClasses;
        }
    }
    
    /**
     * Load all character specs from the characterSpecDAO and store them in 
     * the characterSpecs map in the service.
     * Uses CharacterSpec's id as key
     */    
    private void loadCharacterSpecsFromBackend(){
        Map<String, CharacterSpec> newCharacterSpecs  =   new HashMap<>();
        characterSpecDAO.findAll().forEach(characterSpec -> newCharacterSpecs.put(characterSpecToKey(characterSpec), characterSpec));
        synchronized(characterSpecsLock){
            characterSpecs  =   newCharacterSpecs;
        }
    }
}
