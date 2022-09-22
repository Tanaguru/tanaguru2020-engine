package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface AuditSchedulerRepository extends JpaRepository<AuditScheduler, Long> {
    Optional<AuditScheduler> findByAudit(Audit audit);
    Optional<AuditScheduler> findByAuditId(long id);
}
