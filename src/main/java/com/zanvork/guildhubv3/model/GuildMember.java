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
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zanvork
 */
@Entity
@JsonIgnoreProperties("guild")
public class GuildMember implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    
    @Getter
    @Setter
    @ManyToOne
    private Guild guild;
    
    @Getter
    @Setter
    @JoinColumn(unique = true)
    @OneToOne(cascade = CascadeType.ALL)
    private WarcraftCharacter member;
    
    @Getter
    @Setter
    private int rank;
}
