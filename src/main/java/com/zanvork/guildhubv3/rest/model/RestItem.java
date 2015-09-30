package com.zanvork.guildhubv3.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestItem {
    private long id;
    private String name;
    private String description;
    private String icon;
    private int quality;
    private int itemLevel;
    private List<RestStat> stats;
    private List<RestStat> bonusStats;
    private int armor;
    private String context;
    private List<Integer> bonusLists;
}
