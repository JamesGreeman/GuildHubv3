package com.zanvork.guildhubv3.services;

import com.zanvork.guildhubv3.model.CharacterClass;
import com.zanvork.guildhubv3.model.CharacterSpec;
import com.zanvork.guildhubv3.model.Realm;
import com.zanvork.guildhubv3.model.dao.CharacterClassDAO;
import com.zanvork.guildhubv3.model.dao.CharacterSpecDAO;
import com.zanvork.guildhubv3.model.dao.RealmDAO;
import java.util.HashMap;
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
    @Autowired
    private RealmDAO realmDAO;
    @Autowired
    private CharacterClassDAO characterClassDAO;
    @Autowired
    private CharacterSpecDAO characterSpecDAO;
    
    private Map<String, Realm> realms                   =   new HashMap<>();
    private Map<Long, CharacterClass> characterClasses  =   new HashMap<>();
    private Map<Long, CharacterSpec> characterSpecs     =   new HashMap<>();
    
    private final Object 
            realmsLock          =   new Object(), 
            characterClassesLock  =   new Object(), 
            characterSpecsLock   =   new Object();
    
    private String realmToKey(Realm realm){
        return realmNameRegionToKey(realm.getName(),realm.getRegion().name());
    }
    private String realmNameRegionToKey(String realmName, String region){
        return realmName.toLowerCase() + "_" + region.toLowerCase();
    }
    
    private void importRealmsFromRest(){
        
    }
    
    private void importCharacterClassesFromRest(){
        
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
        Map<Long, CharacterSpec> newCharacterSpecs  =   new HashMap<>();
        characterSpecDAO.findAll().forEach(characterSpec -> newCharacterSpecs.put(characterSpec.getId(), characterSpec));
        synchronized(characterSpecsLock){
            characterSpecs  =   newCharacterSpecs;
        }
    }
    
    /**
     * Loads objects from the Blizzard Rest API service.
     * Objects are loaded via a call to the Blizzard REST API and stored in the 
     * backend database
     */
    @Scheduled(fixedDelay=TIME_1_HOUR)
    private void updateFromRest(){
        importRealmsFromRest();
        importCharacterClassesFromRest();
    }
    
    /**
     * Loads object from the backend database into memory.
     */
    @Scheduled(fixedDelay=TIME_1_SECOND)
    private void updateFromBackend(){
        loadRealmsFromBackend();
        loadCharacterClassesFromBackend();
        loadCharacterSpecsFromBackend();
    }
    
}
