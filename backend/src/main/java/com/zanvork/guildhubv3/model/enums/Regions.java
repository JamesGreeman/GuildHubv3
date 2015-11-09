package com.zanvork.guildhubv3.model.enums;

/**
 *
 * @author zanvork
 */
public enum Regions {
    EU("eu"), 
    US("us"),
    KR("kr"),
    TW("tw");
    
    private final String value;
    
    private Regions(String value){
        this.value  =   value;
    }
    
    @Override
    public String toString(){
        return value;
    }    
}
