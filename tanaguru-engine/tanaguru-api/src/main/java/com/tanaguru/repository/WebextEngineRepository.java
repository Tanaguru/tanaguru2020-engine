package com.tanaguru.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanaguru.domain.entity.audit.WebextEngine;

/**
 * 
 * @author lpedrau
 *
 */
@Repository
public interface WebextEngineRepository extends JpaRepository<WebextEngine, Long>  {

    Optional<WebextEngine> findByEngineVersion(String engineVersion);
    
}
