package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.Guild;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface GuildDAO extends CrudRepository<Guild, Long> {
    
}
