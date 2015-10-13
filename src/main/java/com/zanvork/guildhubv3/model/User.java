package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;

/**
 *
 * @author zanvork
 */
@Entity
@Data
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    
    @Column(name="username", nullable = false, unique = true)
    private String username;
    
    @Column(name="email", nullable = false, unique = true)
    private String emailAddress;
    
    @Column(name="password", nullable = false)
    private String passwordHash;
    
    @Column(name="enabled", nullable = false)
    private boolean enabled;
        
	@JsonIgnore
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
	private Set<Role> roles = new HashSet<>();
    
    public User(){
        
    }
    
    public User(User user){
        id              =   user.getId();
        username        =   user.getUsername();
        emailAddress    =   user.getEmailAddress();
        passwordHash    =   user.getPasswordHash();
        enabled         =   user.isEnabled();
        roles           =   user.getRoles();
    }
    
}
