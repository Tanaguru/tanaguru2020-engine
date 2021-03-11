package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    Collection<Audit> findAllByDeletedIsTrue();

    Optional<Audit> findById(Long id);
}
