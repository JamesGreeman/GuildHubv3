package com.zanvork.battlenet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestGlyph {
    @JsonProperty(value="glyph")
    private long id;
    private int item;
    private String name;
    private String icon;
}