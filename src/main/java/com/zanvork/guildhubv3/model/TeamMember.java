package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zanvork
 */
@Entity
@JsonIgnoreProperties("team")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"team", "member"}))
public class TeamMember implements Serializable {
    @Getter
    @Setter
    @Id
    @GeneratedValue
    private long id;
    
    @Getter
    @Setter
    @ManyToOne
    private Team team;
    
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    private WarcraftCharacter member;
}
