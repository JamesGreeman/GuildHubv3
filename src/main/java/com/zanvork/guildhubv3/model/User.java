package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author zanvork
 */

@Data
@EqualsAndHashCode(exclude={"characters", "guilds", "teams", "ownershipRequests", "teamInvites"})
@ToString(exclude={"passwordHash", "characters", "guilds", "teams", "ownershipRequests", "teamInvites"})
@JsonIgnoreProperties({"passwordHash", "password", "characters", "guilds", "teams", "ownershipRequests", "teamInvites"} )
@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue
    @Column(name="user_id")
    private long id;
    
    @Column(name="username", nullable = false, unique = true)
    private String username;
    
    @Column(name="email", nullable = false, unique = true)
    private String emailAddress;
    
    @Column(name="password", nullable = false)
    private String passwordHash;
    
    @Column(name="enabled", nullable = false)
    private boolean enabled;
        
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = { @JoinColumn(name = "role_id") })
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<WarcraftCharacter> characters;
    
    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<Guild> guilds;
    
    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    private Set<Team> teams;
    
    @OneToMany(mappedBy = "currentOwnerId", fetch = FetchType.EAGER)
    private Set<OwnedEntityOwnershipRequest> ownershipRequests;
    
    @OneToMany(mappedBy = "characterOwnerId", fetch = FetchType.EAGER)
    private Set<TeamInvite> teamInvites;
    
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
    
    public boolean hasRole(String roleName){
        return roles.stream().anyMatch((role) -> (role.getName().equals(roleName)));
    }
    
}
