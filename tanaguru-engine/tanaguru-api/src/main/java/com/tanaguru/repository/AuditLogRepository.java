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
    Page<AuditLog> findAllByAuditAndLevel(Audit audit, EAuditLogLevel level, Pageable pageable);
    
    @Query("select e from AuditLog e where e.audit = ?1 and year(e.date) = ?2 and month(e.date) = ?3 and day(e.date)= ?4 ")
    Page<AuditLog> findAllByAuditAndDate(Audit audit, int year, int month, int day, Pageable pageable);
    
    @Query("select e from AuditLog e where e.audit = ?1 and e.level = ?2 and year(e.date) = ?3 and month(e.date) = ?4 and day(e.date)= ?5 ")
    Page<AuditLog> findAllByAuditAndLevelAndDate(Audit audit, EAuditLogLevel level, int year, int month, int day , Pageable pageable);

}
