package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.Character;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface CharacterDAO extends CrudRepository<Character, Long>{
    
}
