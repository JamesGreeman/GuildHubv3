package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;

/**
 *
 * @author zanvork
 */
@Entity
@Data
public class UserToken implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    
    @ManyToOne
    private User user;
    
    private String tokenHash;
}
