package com.zanvork.battlenet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestTalent {
    private long id;
    private int tier;
    @JsonProperty(value="column")
    private int talentColumn;
    private RestSpell spell;
}
