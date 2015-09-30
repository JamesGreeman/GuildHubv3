package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.User;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface UserDAO extends CrudRepository<User, Long>{
    
}
