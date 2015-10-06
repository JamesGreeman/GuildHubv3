package com.zanvork.guildhubv3.model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.Data;

/**
 *
 * @author zanvork
 */
@Entity
@Data
public class TeamMember implements Serializable {
    
    @Id
    @GeneratedValue
    private long id;
    
    @ManyToOne
    private Team team;
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private WarcraftCharacter member;
}
