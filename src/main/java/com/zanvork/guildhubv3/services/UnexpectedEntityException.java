package com.zanvork.guildhubv3.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author zanvork
 */
@ResponseStatus(value=HttpStatus.CONFLICT, reason = "Unexpected entity")
public class UnexpectedEntityException extends RuntimeException {
    public UnexpectedEntityException(String message){
        super(message);
    }
}
