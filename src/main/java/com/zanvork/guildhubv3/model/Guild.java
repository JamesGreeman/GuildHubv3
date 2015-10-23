package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import java.util.ArrayList;
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

/**
 *
 * @author zanvork
 */

@Data
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class Guild implements Serializable, OwnedEntity {
    
    @Id 
    @GeneratedValue
    protected long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne 
    @JoinColumn(nullable = false)
    private Realm realm;
    
    @OneToMany(mappedBy = "guild", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    private List<GuildMember> members  =   new ArrayList<>();
    
    @JoinColumn(nullable = false)
    @OneToOne(cascade = CascadeType.ALL)
    private WarcraftCharacter leader;
    
    @ManyToOne
    private User owner;
    
    private boolean readOnly;
    
    private boolean ownershipLocked;
    
    public void addMember(GuildMember member){
        members.add(member);
    }
   
}
