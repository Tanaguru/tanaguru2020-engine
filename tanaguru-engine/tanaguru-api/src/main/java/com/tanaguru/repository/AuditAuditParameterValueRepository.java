package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.parameter.AuditAuditParameterValue;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface AuditAuditParameterValueRepository extends JpaRepository<AuditAuditParameterValue, Long>  {
    @Transactional
    void deleteAllByAudit(Audit audit);
    Collection<AuditAuditParameterValue> findAllByAudit(Audit audit);
}
