package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.User;
import java.security.Principal;
import org.springframework.security.core.Authentication;

/**
 *
 * @author zanvork
 */
public abstract class RESTController {
    protected User getActiveUser(Principal principal){
        User activeUser = (User) ((Authentication) principal).getPrincipal();
        return activeUser;
    }
}
