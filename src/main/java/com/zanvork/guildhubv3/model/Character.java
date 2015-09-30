package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.ToString;

/**
 *
 * @author zanvork
 */
@Entity
@Data 
@ToString(exclude = {"guild"})
@Table(name="character", uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class Character implements Serializable {
    @Id 
    @GeneratedValue
    private long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne 
    @Column(nullable = false)
    private Realm realm;
    
    private int averageItemLevel;
    
    @ManyToOne 
    @Column(nullable = false)
    private CharacterClass characterClass;
    
    @ManyToOne 
    @Column(nullable = false)
    private CharacterSpec mainSpec;
    
    @ManyToOne
    private CharacterSpec offSpec;
    
    @OneToMany(mappedBy = "character")
    private List<CharacterItem> items;
    
    @ManyToOne 
    @Column(nullable = false)
    private User owner;
    
    @ManyToOne 
    private Guild guild;
}
