package com.zanvork.battlenet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestGlyphs {
    private List<RestGlyph> major;
    private List<RestGlyph> minor;
}
