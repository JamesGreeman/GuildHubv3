package com.zanvork.guildhubv3.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.CONFLICT, reason = "Entity already exists")
public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String message){
        super(message);
    }
    
}
