package com.zanvork.guildhubv3.blizzardAPIprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCharacter {

    private long lastModified;
    private String name;
    private String realm;
    private String battlegroup;

    @JsonProperty(value = "class")
    private int charClass;
    private int race;
    private int gender;
    
    @JsonProperty(value = "level")
    private int charLevel;
    private int achievementPoints;
    private String thumbnail;
    private String calcClass;
    private RestCharacterItems items;
    private List<RestCharacterTalents> talents;
    private long totalHonorableKills;

}
