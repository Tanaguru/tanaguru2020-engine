package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author rcharre
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    org.springframework.data.domain.Page<Audit> findAll(Pageable pageable);
    Collection<Audit> findAllByDeletedIsTrue();
    
    @Query(value = "SELECT count(*) FROM Audit WHERE type = :type ")
    int numberOfAuditByType(@Param("type") EAuditType type);
}
