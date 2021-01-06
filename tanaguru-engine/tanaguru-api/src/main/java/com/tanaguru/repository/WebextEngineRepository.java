package com.tanaguru.repository;

import java.util.Collection;

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

    Collection<WebextEngine> findAllByEngineVersion(String engineVersion);
    
}
