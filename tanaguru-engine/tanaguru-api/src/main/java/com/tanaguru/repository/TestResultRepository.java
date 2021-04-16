package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author rcharre
 */
@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    /**
     * Find all @see TestResult for a given @see Page
     *
     * @param page The @see Page
     * @return The collection of @see TestResult
     */
    Collection<TestResult> findAllByPage(Page page);


    Collection<TestResult> findAllByPageAndReferencesContaining(Page page, TestHierarchy testHierarchy);
}
