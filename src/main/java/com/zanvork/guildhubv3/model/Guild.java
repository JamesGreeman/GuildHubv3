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
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zanvork
 */
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name", "realm"}))
public class Guild implements Serializable {
    @Id
    @Getter
    @Setter
    @GeneratedValue      
    private long id;
    
    @Column(nullable = false)
    @Getter
    @Setter
    private String name;
    
    @ManyToOne 
    @JoinColumn(nullable = false)
    @Getter
    @Setter
    private Realm realm;
    
    @Getter
    @Setter
    @OneToMany(mappedBy = "guild", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<GuildMember> members  =   new ArrayList<>();
    
    @Getter
    @Setter
    @JoinColumn(nullable = false)
    @OneToOne(cascade = CascadeType.ALL)
    private WarcraftCharacter leader;
    
    @ManyToOne
    @Getter
    @Setter
    private User owner;
    
    public void addMember(GuildMember member){
        members.add(member);
    }
}
