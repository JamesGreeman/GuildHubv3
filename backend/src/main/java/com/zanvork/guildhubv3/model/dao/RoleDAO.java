package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author zanvork
 */
public interface RoleDAO extends JpaRepository<Role, Long>{
    
    Optional<Role> findOneByName(String name);
    
}
