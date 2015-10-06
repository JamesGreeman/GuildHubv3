package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@JsonIgnoreProperties("guild")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class WarcraftCharacter implements Serializable {
    @Id 
    @GeneratedValue
    private long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne 
    @JoinColumn(nullable = false)
    private Realm realm;
    
    private int averageItemLevel;
    
    @ManyToOne 
    @JoinColumn(nullable = false)
    private CharacterClass characterClass;
    
    @ManyToOne 
    @JoinColumn(nullable = false)
    private CharacterSpec mainSpec;
    
    @ManyToOne
    private CharacterSpec offSpec;
    
    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<CharacterItem> items;
    
    @ManyToOne
    private User owner;
    
    @ManyToOne
    private Guild guild;
}
