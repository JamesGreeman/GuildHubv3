package com.zanvork.guildhubv3.blizzardAPIprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestTalentSpec implements Serializable {
    private long    id;
    private String  name;
    
    @JsonProperty(value="role")
    private String  specRole;
    private String  backgroundImage;
    private String  icon;
    private String  description;
    
    @JsonProperty(value="order")
    private int     specOrder;
}
