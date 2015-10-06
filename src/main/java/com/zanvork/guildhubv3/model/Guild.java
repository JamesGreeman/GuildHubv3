package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class Guild implements Serializable {
    @Id 
    @GeneratedValue      
    private long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne 
    @JoinColumn(nullable = false)
    private Realm realm;
    
    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL)
    private List<WarcraftCharacter> members;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private WarcraftCharacter leader;
    
    @ManyToOne
    private User owner;
}
