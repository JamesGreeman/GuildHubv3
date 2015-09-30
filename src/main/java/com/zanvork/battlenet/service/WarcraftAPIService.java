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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jgreeman
 */
@Service
public class WarcraftAPIService {
    
    
    /**
     * Load information for a specified character
     * @param region region the character is in
     * @param realm realm the character is in
     * @param name name of the character
     * @return the character object
     */
    public RestCharacter getCharacter(String region, String realm, String name){
        return new RestTemplate().getForObject(
                BattleNetRequest.buildRequest("character", region, realm, name, new String[]{"talents","items"}),
                RestCharacter.class
        );
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
        RestGuild guild = new RestTemplate().getForObject(
                BattleNetRequest.buildRequest("guild", region, realm, name, new String[]{"members"}),
                RestGuild.class
        );
        if (getMemberDetails){
            guild.getMembers().stream().forEach((member) -> {
                member.setGuildMember(getCharacter(region, guild.getRealm(), member.getGuildMember().getName()));
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
        return new RestTemplate().getForObject(
                BattleNetRequest.buildObjectRequest("realm", region), 
                RestRealms.class
        ).getRealms();
    }  
    
    /**
     * Load all classes.
     * @return list of RestClass models
     */
    public List<RestClass> getClasses(){
        return new RestTemplate().getForObject(
                BattleNetRequest.buildObjectRequest("data/character/classes"), 
                RestClasses.class
        ).getClasses();
    }
    /**
     * Load all races.
     * @return list of RestRace models
     */
    public List<RestRace> getRaces(){
        return new RestTemplate().getForObject(
                BattleNetRequest.buildObjectRequest("data/character/races"), 
                RestRaces.class
        ).getRaces();
    }
    
    public RestItem getItem(long id){
        return new RestTemplate().getForObject(
                BattleNetRequest.buildObjectRequest("item", "", "","" + id), 
                RestItem.class
        );
    }
}
