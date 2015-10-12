package com.zanvork.guildhubv3.services;

import static com.zanvork.guildhubv3.services.BackendService.TIME_15_SECOND;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author zanvork
 */
@Service
public class UserService implements BackendService{
    
    /**
     * Store all objects currently cached in service.
     */
    @Scheduled(fixedDelay=TIME_15_SECOND)
    @Override
    public void updateToBackend(){
        //TODO: implement this
    }
    /**
     * Loads object from the backend database into memory.
     */
    @Scheduled(fixedDelay=TIME_15_SECOND)
    @Override
    public void updateFromBackend(){
        //TODO: implement this
    }
}