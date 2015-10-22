package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@EqualsAndHashCode(exclude="team")
@ToString(exclude="team")
@JsonIgnoreProperties("team")
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"team", "member"}))
public class TeamMember implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    
    @ManyToOne
    private Team team;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private WarcraftCharacter member;
}
