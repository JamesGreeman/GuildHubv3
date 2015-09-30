package com.zanvork.guildhubv3.model;

import com.zanvork.guildhubv3.model.enums.Regions;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;

/**
 *
 * @author zanvork
 */
@Entity
@Data
@Table(name="team", uniqueConstraints=@UniqueConstraint(columnNames={"name", "region"}))
public class Team {
    
    @Id
    @GeneratedValue
    private long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('EU', 'US', 'KR', 'TW')")
    private Regions region;
    
    @OneToMany(mappedBy = "team")
    private List<TeamMember> members;
    
    @ManyToOne
    private User owner;
}
