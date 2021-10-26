package com.tanaguru.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.audit.Webextention;
import com.tanaguru.domain.entity.membership.user.User;

/**
 * 
 * @author lpedrau
 *
 */
public interface WebextentionRepository extends JpaRepository<Webextention, Long> {

    Optional<Webextention> findFirstByOrderByIdDesc();
    
    Optional<Webextention> findByTestHierarchy(TestHierarchy testHierarchy);
}
