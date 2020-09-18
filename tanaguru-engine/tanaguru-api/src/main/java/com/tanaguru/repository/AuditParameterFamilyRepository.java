package com.tanaguru.repository;

import com.tanaguru.domain.constant.EParameterFamily;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface AuditParameterFamilyRepository extends JpaRepository<AuditParameterFamily, Long> {
    Optional<AuditParameterFamily> findByCode(EParameterFamily code);
}
