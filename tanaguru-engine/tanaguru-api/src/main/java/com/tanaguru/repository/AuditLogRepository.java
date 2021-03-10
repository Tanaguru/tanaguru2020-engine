package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;

/**
 * @author rcharre
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Collection<AuditLog> findAllByAudit(Audit audit);
    Page<AuditLog> findAllByAudit(Audit audit, Pageable pageable);
    Page<AuditLog> findAllByAuditAndLevel(Audit audit, EAuditLogLevel level, Pageable pageable);
    Page<AuditLog> findAllByAuditAndDate(Audit audit, Date date, Pageable pageable);
    Page<AuditLog> findAllByAuditAndLevelAndDate(Audit audit, EAuditLogLevel level,Date date ,Pageable pageable);

}
