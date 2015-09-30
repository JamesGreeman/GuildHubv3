package com.zanvork.guildhubv3.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCharacterTalents{
    private boolean selected    =   false;
    private List<RestTalent> talents;
    private RestGlyphs glyphs;
    private RestTalentSpec spec;
    private String calcTalent;
    private String calcSpec;
    private String calcGlyph;
}
