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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author zanvork
 */

@Data
@EqualsAndHashCode(exclude="guildMember", callSuper=false)
@ToString(exclude="guildMember")
@JsonIgnoreProperties("guildMember")
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class WarcraftCharacter  implements Serializable, OwnedEntity {
    
    @Id 
    @GeneratedValue
    protected long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne 
    @JoinColumn(nullable = false)
    private Realm realm;
    
    private int averageItemLevel;
    
    @ManyToOne 
    private CharacterClass characterClass;
    
    @ManyToOne 
    private CharacterSpec mainSpec;
    
    @ManyToOne
    private CharacterSpec offSpec;
    
    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    private List<CharacterItem> items;
    
    @OneToOne(mappedBy = "member")
    private GuildMember guildMember;
    
    @ManyToOne
    private User owner;
    
    private boolean readOnly;
    
    private boolean ownershipLocked;
    
}
