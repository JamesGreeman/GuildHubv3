package com.zanvork.guildhubv3.model;

import com.zanvork.guildhubv3.model.enums.Regions;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 *
 * @author zanvork
 */

@Data
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"slug", "region"}))
public class Realm implements Serializable {
    
    @Id 
    @GeneratedValue 
    private long id; 
    
    @Column(nullable = false)
    private String name; 
    
    @Column(nullable = false)
    private String slug;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('EU', 'US', 'KR', 'TW')")
    private Regions region;
    
}
