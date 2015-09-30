package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;

/**
 *
 * @author zanvork
 */
@Entity
@Data
public class User implements Serializable {
    @Id 
    @GeneratedValue 
    private long id;
    
    @Column(nullable = false, unique = true) 
    private String userName;
    
    @Column(nullable = false, unique = true) 
    private String email;
    
    private String passwordHash;
}
