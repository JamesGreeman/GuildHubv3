package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.TeamMember;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface TeamMemberDAO extends CrudRepository<TeamMember, Long>{
    
}
