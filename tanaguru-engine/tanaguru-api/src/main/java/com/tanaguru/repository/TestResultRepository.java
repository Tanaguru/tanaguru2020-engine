package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    Collection<TestResult> findDistinctByPageAndTanaguruTest_In(Page page, Collection<TanaguruTest> tests);

    @Query("SELECT DISTINCT tr FROM TestResult tr " +
            "INNER JOIN TanaguruTest tt ON tr.tanaguruTest=tt " +
            "INNER JOIN tt.testHierarchies th WHERE tr.page=:page and th.reference=:reference")
    Collection<TestResult> findTestResultByReference(@Param("page")Page page, @Param("reference")TestHierarchy reference);
}
