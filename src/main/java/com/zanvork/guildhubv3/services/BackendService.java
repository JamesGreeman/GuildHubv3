package com.zanvork.guildhubv3.services;


/**
 *
 * @author zanvork
 */
public interface BackendService {
    public final int TIME_1_HOUR    =   3600000;
    public final int TIME_5_SECOND  =   5000;
    public final int TIME_1_SECOND  =   1000;
    
    
    public void updateFromBackend();
    public void storeObjects();
}
