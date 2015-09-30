package com.zanvork.guildhubv3.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCharacterItems{
    private int averageItemLevel;
    private int averageItemLevelEquipped;
    
    private RestItem head;
    private RestItem neck;
    private RestItem shoulder;
    private RestItem back;
    private RestItem chest;
    private RestItem shirt;
    private RestItem wrist;
    private RestItem hands;
    private RestItem waist;
    private RestItem legs;
    private RestItem feet;
    private RestItem finger1;
    private RestItem finger2;
    private RestItem trinket1;
    private RestItem trinket2;
    private RestItem mainHand;
    private RestItem offHand;
    
    
    
    public Map<String, RestItem> getItems(){
        Map<String, RestItem> items =   new HashMap<>();
        items.put("head", head);
        items.put("neck", neck);
        items.put("shoulder", shoulder);
        items.put("back", back);
        items.put("chest", chest);
        items.put("shirt", shirt);
        items.put("wrist", wrist);
        items.put("hands", hands);
        items.put("waist", waist);
        items.put("legs", legs);
        items.put("feet", feet);
        items.put("finger1", finger1);
        items.put("finger2", finger2);
        items.put("trinket1", trinket1);
        items.put("trinket2", trinket2);
        items.put("mainHand", mainHand);
        items.put("offHand", offHand);
        
        return items;
    }
}
