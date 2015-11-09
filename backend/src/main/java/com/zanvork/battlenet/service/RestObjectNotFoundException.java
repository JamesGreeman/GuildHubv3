package com.zanvork.battlenet.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason = "Unable to load entity from external REST service")
public class RestObjectNotFoundException extends RuntimeException {
    public RestObjectNotFoundException(String message){
        super(message);
    }
}
