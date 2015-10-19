package com.zanvork.guildhubv3.services;

import com.zanvork.guildhubv3.model.Role;
import com.zanvork.guildhubv3.model.User;
import com.zanvork.guildhubv3.model.dao.RoleDAO;
import com.zanvork.guildhubv3.model.dao.UserDAO;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService, BackendService {

    private static final Logger log  =   LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private RoleDAO roleDAO;
        
    private Map<Long, User> users           =   new HashMap();
    private Map<String, User> usersByName   =   new HashMap();
    
    private final Object
            usersLock       =   new Object(),
            usersByNameLock =   new Object();

    
    private final BCryptPasswordEncoder passwordEncoder =   new BCryptPasswordEncoder();
    
    public User getUser(String username){
        synchronized(usersByNameLock){
            return usersByName.get(username);
        }
    }
    
    
    public Role getRole(String name){
        Role role   =   roleDAO.findOneByName(name).orElse(new Role(name));
        if (role.getId() < 1){
            roleDAO.save(role);
        }
        return role;
    }
    
    /**
     * Create a new user account.
     * @param username the new username
     * @param email user's email address
     * @param password user's password
     * @return the new User account
     */    
    public User createUser(String username, String email, String password){
       if (getUser(username ) != null){
           return null;
       }
       User user    =   new User();
       user.setUsername(username);
       user.setEmailAddress(email);
       user.setPasswordHash(passwordEncoder.encode(password));
       user.setEnabled(true);
       Set<Role> roles  =   new HashSet<>();
       roles.add(getRole(Role.ROLE_USER));
       user.setRoles(roles);
       
       saveUser(user);
       return user;
    }
    
    public User updatePassword(String username, String oldPassword, String newPassword){
        User user   =   getUser(username);
        if (user != null){
            if (BCrypt.checkpw(oldPassword, user.getPasswordHash())){
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                saveUser(user);
            } else {
                user = null;
            }
        }
        return user;
    }
     
    public User updatePasswordForUser(String username, String newPassword){
        User user   =   getUser(username);
        if (user != null){
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            saveUser(user);
        } else {
            user = null;
        }
        return user;
    }
    
    
    public void saveUser(User user){
        userDAO.save(user);
        
        synchronized (usersLock){
            users.put(user.getId(), user);
        }
        synchronized (usersByNameLock){
            usersByName.put(user.getUsername(), user);
        }
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDAO.findOneByUsername(username).get();
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
        }
        return new UserRepositoryUserDetails(user);
    }

    public static User getCurrentUser(){
        User user   =   null;
        try {
            user    =   (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e){
            log.error("Could not load user from context", e);
        }
        return user;
    }

    @Scheduled(fixedDelay=TIME_15_SECOND)
    @Override
    public void updateFromBackend() {
           updateUsersFromBackend();
    }
    
    /**
     * Load all guilds from the guildDAO and store them in the guilds map in the service.
     * Uses Guilds's id as key
     */    
    private void updateUsersFromBackend(){
        Map<Long, User>     newUsers          =   new HashMap<>();
        Map<String, User>   newUsersByName    =   new HashMap<>();
        userDAO.findAll().forEach(user -> {
            newUsers.put(user.getId(), user);
            newUsersByName.put(user.getUsername(), user);
        });
        synchronized (usersLock){
            users    =   newUsers;
        }
        synchronized (usersByNameLock){
            usersByName    =   newUsersByName;
        }
    }

    @Override
    public void updateToBackend() {
    }
    
    private final static class UserRepositoryUserDetails extends User implements UserDetails {

        private static final long serialVersionUID = 1L;

        private UserRepositoryUserDetails(User user) {
            super(user);
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return getRoles();
        }

        @Override
        public String getUsername() {
            return super.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getPassword() {
            return getPasswordHash();
        }

    }

}
