package com.zanvork.guildhubv3.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestRealm {
    private String type;
    private String population;
    private String name;
    private String slug;
    private String battlegroup;
    private String locale;
    private String timezone;
    private List<String> connected_realms;
}
