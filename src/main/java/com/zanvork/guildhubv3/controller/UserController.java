package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/signup/{userName}/{email}/{password}", method = RequestMethod.POST)
    public String addUser(@PathVariable String userName, @PathVariable String email, @PathVariable String password){
        if (userService.createUser(userName, email, password) != null){
            return "Successfully created user.";
        }
        return "Failed to create user.";
    }
}
