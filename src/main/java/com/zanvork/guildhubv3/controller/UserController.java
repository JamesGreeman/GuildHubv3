package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.services.UserService;
import java.security.Principal;
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
    
    
    @RequestMapping(value = "/admin/{username}", method = RequestMethod.DELETE)
    public String removeUser(@PathVariable String username, Principal principal){
        return "Not yet implemented";
    }
    
    @RequestMapping(value = "/password/{oldPassword}/{newPassword}", method = RequestMethod.PUT)
    public String changePassword(@PathVariable String oldPassword, @PathVariable String newPassword, Principal principal){
        if (userService.updatePassword(principal.getName(), oldPassword, newPassword) != null){
            return "Successfully updated your password";
        } 
        return "Failed to update your password";
    }
    
    @RequestMapping(value = "/admin/password/{username}/{newPassword}", method = RequestMethod.PUT)
    public String adminChangePassword(@PathVariable String username, @PathVariable String newPassword){
        if (userService.updatePasswordForUser(username, newPassword) != null){
            return "Successfully updated password for " + username;
        } 
        return "Failed to update " + username + "'s password";
        
    }
}
