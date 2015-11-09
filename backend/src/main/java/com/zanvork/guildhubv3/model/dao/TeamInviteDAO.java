package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.TeamInvite;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface TeamInviteDAO extends CrudRepository<TeamInvite, Long>{
    
}
