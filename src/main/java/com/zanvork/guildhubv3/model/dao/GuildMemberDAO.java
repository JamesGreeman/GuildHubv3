package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.GuildMember;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface GuildMemberDAO extends CrudRepository<GuildMember, Long>{
    
}
