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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zanvork
 */
@Entity
@JsonIgnoreProperties("guildMember")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class WarcraftCharacter implements Serializable {
    @Getter
    @Setter
    @Id 
    @GeneratedValue
    private long id;
    
    @Getter
    @Setter
    @Column(nullable = false)
    private String name;
    
    @Getter
    @Setter
    @ManyToOne 
    @JoinColumn(nullable = false)
    private Realm realm;
    
    @Getter
    @Setter
    private int averageItemLevel;
    
    @Getter
    @Setter
    @ManyToOne 
    private CharacterClass characterClass;
    
    @Getter
    @Setter
    @ManyToOne 
    private CharacterSpec mainSpec;
    
    @Getter
    @Setter
    @ManyToOne
    private CharacterSpec offSpec;
    
    @Getter
    @Setter
    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<CharacterItem> items;
    
    @Getter
    @Setter
    @ManyToOne
    private User owner;
    
    @Getter
    @OneToOne(mappedBy = "member")
    private GuildMember guildMember;
    
    
}
