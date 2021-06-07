package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.membership.project.Project;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.stream.Stream;

import javax.persistence.QueryHint;

/**
 * @author rcharre
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    /**
     * Find all page for a given audit id
     *
     * @param auditId The audit Id
     * @return The audit's pages list
     */
    Collection<Page> findAllByAudit_Id(long auditId);

    /**
     * Find all page for a given audit id
     *
     * @param auditId The audit Id
     * @return The audit's pages list
     */
    org.springframework.data.domain.Page<Page> findAllByAudit_Id(long auditId, Pageable pageable);

    org.springframework.data.domain.Page<Page> findByNameContainingIgnoreCaseAndAudit_Id(String name, long auditId, Pageable pageable);

    void deleteAllByAudit(Audit audit);
    
    @Query("select p from Page p")
    Stream<Page> getAll();

}
