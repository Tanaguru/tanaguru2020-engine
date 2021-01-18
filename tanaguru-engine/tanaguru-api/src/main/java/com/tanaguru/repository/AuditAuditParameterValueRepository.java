package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.parameter.AuditAuditParameterValue;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditAuditParameterValueRepository extends JpaRepository<AuditAuditParameterValue, Long>  {
    void deleteAllByAudit(Audit audit);
    Collection<AuditAuditParameterValue> findAllByAudit(Audit audit);
}
