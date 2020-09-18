package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface AuditReferenceRepository extends JpaRepository<AuditReference, Long> {
    Collection<AuditReference> findAllByAudit(Audit audit);
    Optional<AuditReference> findByAuditAndIsMainIsTrue(Audit audit);
    boolean existsByTestHierarchy(TestHierarchy testHierarchy);
}
