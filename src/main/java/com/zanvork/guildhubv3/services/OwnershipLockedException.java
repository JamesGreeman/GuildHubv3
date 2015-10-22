package com.zanvork.guildhubv3.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.FORBIDDEN, reason = "Ownership locked")
public class OwnershipLockedException extends RuntimeException {
    public OwnershipLockedException(String message){
        super(message);
    }
    
}
