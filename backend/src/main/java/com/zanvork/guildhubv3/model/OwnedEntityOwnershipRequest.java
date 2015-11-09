package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author zanvork
 */

@Data
@EqualsAndHashCode(exclude="currentOwner")
@ToString(exclude="currentOwner")
@JsonIgnoreProperties("currentOwner")
@Entity
public class OwnedEntityOwnershipRequest implements Serializable {
    
    @Id
    @GeneratedValue
    private long id;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateCreated;
    
    private long subjectId;
    
    private long currentOwnerId;
    
    private long requesterId;
    
    private String entityType;
    
    
}
