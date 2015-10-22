package com.zanvork.guildhubv3.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.FORBIDDEN, reason = "Read-Only Entity")
public class ReadOnlyEntityException extends RuntimeException {
    public ReadOnlyEntityException(String message){
        super(message);
    }
    
}
