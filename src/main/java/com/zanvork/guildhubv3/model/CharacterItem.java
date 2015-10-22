package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zanvork.guildhubv3.model.enums.ItemSlots;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author zanvork
 */

@Data
@EqualsAndHashCode(exclude="owner")
@ToString(exclude="owner")
@JsonIgnoreProperties("owner")
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"slot", "owner"}))
public class CharacterItem implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    
    
    private long blizzardID;
    
    private int itemLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('HEAD', 'NECK', 'SHOULDER', 'BACK', "
            + "'CHEST', 'SHIRT', 'WRIST', 'HANDS', 'WAIST', 'LEGS', 'FEET', "
            + "'FINGER1', 'FINGER2', 'TRINKET1', 'TRINKET2', 'MAINHAND', 'OFFHAND')")
    private ItemSlots slot;
    
    @ManyToOne
    private WarcraftCharacter owner;
}
