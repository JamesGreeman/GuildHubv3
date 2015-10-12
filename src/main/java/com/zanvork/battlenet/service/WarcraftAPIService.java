package com.zanvork.battlenet.service;

import com.zanvork.battlenet.utils.BattleNetRequest;
import com.zanvork.battlenet.model.RestCharacter;
import com.zanvork.battlenet.model.RestClass;
import com.zanvork.battlenet.model.RestClasses;
import com.zanvork.battlenet.model.RestGuild;
import com.zanvork.battlenet.model.RestItem;
import com.zanvork.battlenet.model.RestRace;
import com.zanvork.battlenet.model.RestRaces;
import com.zanvork.battlenet.model.RestRealm;
import com.zanvork.battlenet.model.RestRealms;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jgreeman
 */
@Service
public class WarcraftAPIService {
    
    
    private final Logger log  =   LoggerFactory.getLogger(this.getClass());
    /**
     * Load information for a specified character
     * @param region region the character is in
     * @param realm realm the character is in
     * @param name name of the character
     * @return the character object
     */
    public RestCharacter getCharacter(String region, String realm, String name){
        String url  =   BattleNetRequest.buildRequest("character", region, realm, name, new String[]{"talents","items"});
        try {
            return new RestTemplate().getForObject(url, RestCharacter.class);
        } catch (HttpClientErrorException e){
            log.error("Could not make request '" + url,  e);
        }
        return null;        
    }
    
    public RestGuild getGuild(String region, String realm, String name){
        return WarcraftAPIService.this.getGuild(region, realm, name, false);
    }
    /**
     * Load information for a specified guild
     * @param region region the guild is in
     * @param realm realm the guild is on
     * @param name name of the guild
     * @param getMemberDetails whether to load details of all guild members
     * @return the guild object
     */
    public RestGuild getGuild(String region, String realm, String name, boolean getMemberDetails){
        RestGuild guild =   null;
        String url  =   BattleNetRequest.buildRequest("guild", region, realm, name, new String[]{"members"});
        try {
            guild   =   new RestTemplate().getForObject(url, RestGuild.class);
        } catch (HttpClientErrorException e){
            log.error("Could not make request '" + url,  e);
        }
        if (getMemberDetails && guild != null){
            guild.getMembers().stream().forEach((member) -> {
                member.setGuildMember(getCharacter(region, member.getGuildMember().getRealm(), member.getGuildMember().getName()));
            });
        }
        return guild;
    }
    
    /**
     * Load all realms for a specified region
     * @param region region to be loaded
     * @return list of RestRealm models
     */
    public List<RestRealm> getRealms(String region){
        String url  =   BattleNetRequest.buildObjectRequest("realm", region);
        try {
            return new RestTemplate().getForObject(url, RestRealms.class).getRealms();
        } catch (HttpClientErrorException e){
            log.error("Could not make request '" + url,  e);
        }
        return null;     
    }  
    
    /**
     * Load all classes.
     * @return list of RestClass models
     */
    public List<RestClass> getClasses(){
        String url  =   BattleNetRequest.buildObjectRequest("data/character/classes");
        try {
            return new RestTemplate().getForObject(url, RestClasses.class).getClasses();
        } catch (HttpClientErrorException e){
            log.error("Could not make request '" + url,  e);
        }
        return null;       
    }
    /**
     * Load all races.
     * @return list of RestRace models
     */
    public List<RestRace> getRaces(){
        String url  =   BattleNetRequest.buildObjectRequest("data/character/races");
        try {
            return new RestTemplate().getForObject(url, RestRaces.class).getRaces();
        } catch (HttpClientErrorException e){
            log.error("Could not make request '" + url,  e);
        }
        return null;       
    }
    
    public RestItem getItem(long id){
        String url  =   BattleNetRequest.buildObjectRequest("item", "", "","" + id);
        try {
            return new RestTemplate().getForObject(url, RestItem.class);
        } catch (HttpClientErrorException e){
            log.error("Could not make request '" + url,  e);
        }
        return null;    
    }
}
