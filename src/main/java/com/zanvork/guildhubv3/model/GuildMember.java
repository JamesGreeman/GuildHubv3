package com.zanvork.guildhubv3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 *
 * @author zanvork
 */

@Data
@EqualsAndHashCode(exclude="guild")
@ToString(exclude="guild")
@JsonIgnoreProperties("guild")
@Entity
public class GuildMember implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    
    @ManyToOne
    private Guild guild;
    
    @JoinColumn(unique = true)
    @OneToOne(cascade = CascadeType.ALL)
    private WarcraftCharacter member;
    
    private int rank;
}
