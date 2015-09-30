package com.zanvork.guildhubv3.model;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 *
 * @author zanvork
 */
@Entity
@Data
@Table(name="guild", uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class Guild {
    @Id 
    @GeneratedValue      
    private long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne 
    @Column(nullable = false)
    private Realm realm;
    
    @OneToMany(mappedBy = "guild")
    private List<Character> members;
    
    @OneToOne 
    @Column(nullable = false)
    private Character leader;
    
    @ManyToOne 
    private User owner;
}
