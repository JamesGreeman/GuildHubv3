package com.zanvork.guildhubv3.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason = "Not authenticated")
public class NotAuthenticatedException extends RuntimeException {
    public NotAuthenticatedException(String message){
        super(message);
    }
}
