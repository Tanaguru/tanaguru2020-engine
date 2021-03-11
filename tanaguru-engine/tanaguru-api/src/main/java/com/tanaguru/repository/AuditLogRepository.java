package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author rcharre
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Collection<AuditLog> findAllByAudit(Audit audit);
    Page<AuditLog> findAllByAudit(Audit audit, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.audit = ?1 AND a.level IN ?2")
    Page<AuditLog> findAllByAuditAndLevel(Audit audit, Collection<EAuditLogLevel> levels, Pageable pageable);
}
