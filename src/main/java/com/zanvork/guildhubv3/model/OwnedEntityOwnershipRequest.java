package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lombok.Data;

/**
 *
 * @author zanvork
 */

@Data
@Entity
public class OwnedEntityOwnershipRequest implements Serializable {
    
    @Id
    @GeneratedValue
    private long id;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateCreated;
    
    private long subjectId;
    
    @ManyToOne
    private User currentOwner;
    @ManyToOne
    private User requester;
    
    private String entityType;
    
    
}
