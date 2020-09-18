package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface AuditParameterRepository extends JpaRepository<AuditParameter, Long> {
    /**
     * Find an auditParameter list from an AuditParameterFamily
     *
     * @param family The given AuditParameterFamily
     * @return An AuditParameter list
     */
    Collection<AuditParameter> findByAuditParameterFamily(AuditParameterFamily family);

    /**
     * Find an auditParameter for a given code
     *
     * @param code The given code
     * @return The corresponding auditParameter
     */
    Optional<AuditParameter> findByCode(String code);
}
