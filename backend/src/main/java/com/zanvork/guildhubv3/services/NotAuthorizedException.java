package com.zanvork.guildhubv3.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.FORBIDDEN, reason = "Not Authorized")
public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String message){
        super(message);
    }
    
}
