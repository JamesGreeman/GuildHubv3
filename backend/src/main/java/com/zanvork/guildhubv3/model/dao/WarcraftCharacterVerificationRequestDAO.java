package com.zanvork.guildhubv3.model.dao;

import com.zanvork.guildhubv3.model.WarcraftCharacterVerificationRequest;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author zanvork
 */
public interface WarcraftCharacterVerificationRequestDAO extends CrudRepository<WarcraftCharacterVerificationRequest, Long>{
    
}
