package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 *
 * @author zanvork
 */

public interface OwnedEntity {

    public User getOwner();

    public void setOwner(User owner);

    public boolean isOwnershipLocked() ;
    
    public void setOwnershipLocked(boolean ownershipLocked);

    public boolean isReadOnly() ;

    public void setReadOnly(boolean readOnly);
    
}
