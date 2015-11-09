package com.zanvork.guildhubv3.model;

import com.zanvork.guildhubv3.model.enums.ItemSlots;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
public class WarcraftCharacterVerificationRequest implements Serializable {
    
    @Id
    @GeneratedValue
    private long id;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateCreated;
    
    private long subjectId;
    
    private long requesterId;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('HEAD', 'NECK', 'SHOULDER', 'BACK', "
            + "'CHEST', 'SHIRT', 'WRIST', 'HANDS', 'WAIST', 'LEGS', 'FEET', "
            + "'FINGER1', 'FINGER2', 'TRINKET1', 'TRINKET2', 'MAINHAND', 'OFFHAND')")
    private ItemSlots slot;
    
    
}
