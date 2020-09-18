package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.StatusResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface StatusResultRepository extends JpaRepository<StatusResult, Long> {
    /**
     * Find a @see StatusResult for a given @see Page and a @see TestHierarchy
     * @param page The @see Page
     * @param reference The @see TestHierarchy
     * @return The @see StatusResult
     */
    Optional<StatusResult> findByReferenceAndPage(TestHierarchy reference, Page page);

    /**
     * Find a collection of  @see StatusResult for a given @see Audit and a @see TestHierarchy
     * @param audit The @see Audit
     * @param reference The @see TestHierarchy
     * @return The collection of @see StatusResult
     */
    Collection<StatusResult> findAllByReferenceAndPage_Audit(TestHierarchy reference, Audit audit);
}
