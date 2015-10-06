package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.WarcraftCharacter;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface WarcraftCharacterDAO extends CrudRepository<WarcraftCharacter, Long>{
    
}
