package com.zanvork.guildhubv3.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason = "No such entity")
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message){
        super(message);
    }
    
}
