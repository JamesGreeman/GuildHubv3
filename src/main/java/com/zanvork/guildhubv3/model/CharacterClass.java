package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

/**
 *
 * @author zanvork
 */
@Entity
@Data
public class CharacterClass implements Serializable {
    @Id
    private long id;
    
    @Column(nullable = false) 
    private String className;
}
