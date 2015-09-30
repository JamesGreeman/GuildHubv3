package com.zanvork.guildhubv3.blizzardAPIprocessor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestRealms {
    private List<RestRealm> realms;
}
