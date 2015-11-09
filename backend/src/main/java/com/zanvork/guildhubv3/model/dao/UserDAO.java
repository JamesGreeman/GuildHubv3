package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author zanvork
 */
public interface UserDAO extends JpaRepository<User, Long>{
    
    Optional<User> findOneByUsername(String userName);
    
}
