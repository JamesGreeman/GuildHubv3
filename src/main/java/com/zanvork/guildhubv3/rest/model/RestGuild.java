package com.zanvork.guildhubv3.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestGuild {
    private long    lastModified;
    private String  name;
    private String  realm;
    private String  battlegroup;
    
    @JsonProperty(value="level")
    private int     guildLevel;
    private int     side;
    private int     achievementPoints;
    private List<RestGuildMember> members;
        
}
