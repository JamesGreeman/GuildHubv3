package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;

/**
 *
 * @author zanvork
 */

@Data
@Entity
public class CharacterSpec implements Serializable {
    @Id 
    @GeneratedValue
    private long id;
    
    @Column(nullable = false) 
    private String specName; 
    
    @ManyToOne
    private CharacterClass characterClass;
    
}
