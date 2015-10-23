package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.Team;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface TeamDAO extends CrudRepository<Team, Long> {
    
}
