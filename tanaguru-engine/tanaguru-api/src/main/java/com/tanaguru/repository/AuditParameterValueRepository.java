package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface AuditParameterValueRepository extends JpaRepository<AuditParameterValue, Long> {
    Optional<AuditParameterValue> findByAuditParameter_CodeAndValue(EAuditParameter auditParameter, String value);

    Optional<AuditParameterValue> findFirstByIsDefaultAndAuditParameter(boolean isDefault, AuditParameter auditParameter);
}
