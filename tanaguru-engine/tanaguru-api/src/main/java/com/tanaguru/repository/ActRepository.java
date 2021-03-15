package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ActRepository extends JpaRepository<Act, Long> {
    Optional<Act> findByAudit(Audit audit);

    Collection<Act> findAllByProject(Project project);
    
    Collection<Act> findAllByProjectAndAudit_Type(Project project, EAuditType type);

    org.springframework.data.domain.Page<Act> findAll(Pageable pageable);
    org.springframework.data.domain.Page<Act> findAllByProject(Project project, Pageable pageable);

    /**
     * Find last @see Act for a given @see Project
     * @param project The @see Project
     * @return The @see Act
     */
    Optional<Act> findFirstByProjectOrderByDateDesc(Project project);

    /**
     * Find last @see Act for a given @see Project and a given @see EAuditType
     * @param project The @see Project
     * @param type The @see EAuditType
     * @return The @see Act
     */
    Optional<Act> findFirstByProjectAndAudit_TypeOrderByDateDesc(Project project, EAuditType type);
}