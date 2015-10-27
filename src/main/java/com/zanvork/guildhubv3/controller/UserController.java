package com.zanvork.guildhubv3.controller;

import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.services.UserService;
import java.security.Principal;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author zanvork
 */
@RestController
@RequestMapping("/users")
public class UserController extends RESTController {
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(method  =   RequestMethod.GET)
    public UserResponse getUser(
            Principal p){
        
        User user   =   getActiveUser(p);
        UserResponse response   =   new UserResponse(user);
        
        return response;
    }
    
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public UserResponse addUser(
            @RequestBody SignupRequest r){
        
        String username =   r.getUsername();
        String email    =   r.getEmail();
        String password =   r.getPassword();
        
        User user   =   userService.createUser(username, email, password);
        UserResponse response   =   new UserResponse(user);
        
        return response;
    }
    
    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(
            final Principal p,
            final @RequestBody ChangePasswordRequest r){
        
        String oldPassword  =   r.getOldPassword();
        String newPassword  =   r.getOldPassword();
        
        User user           =   getActiveUser(p);
        
        userService.updatePassword(user, oldPassword, newPassword);
    }
    
    //GET /summary
    
    @RequestMapping(value = "/admin/{userId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeUser(
            @PathVariable long userId){
        
        userService.deleteUser(userId);
    }
    
    
    @RequestMapping(value = "/admin/password/{username}/{newPassword}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void adminChangePassword(
            final @RequestBody AdminChangePasswordRequest r){
        
        long userId         =   r.userId;
        String newPassword  =   r.getNewPassword();
        
        userService.updatePasswordForUser(userId, newPassword);
        
    }
    
    //Request Objects
    @Data
    protected class SignupRequest{
        private String username;
        private String email;
        private String password;
    }
   
    @Data
    protected class ChangePasswordRequest{
        private String oldPassword;
        private String newPassword;
    }
    @Data
    protected class AdminChangePasswordRequest{
        private long userId;
        private String newPassword;
    }
 
}
