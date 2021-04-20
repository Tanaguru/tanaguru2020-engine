package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query(value = "SELECT DISTINCT tr FROM test_result tr " +
            "INNER JOIN tanaguru_test tt ON tr.tanaguru_test_id=tt.id " +
            "INNER JOIN test_hierarchy_tanaguru_test thtt ON tt.id=thtt.tanaguru_test_id " +
            "INNER JOIN test_hierarchy th ON th.id=thtt.test_hierarchy_id " +
            "WHERE tr.page_id=?1 AND th.reference_id=?2;", nativeQuery = true)
    Collection<TestResult> findTestResultByReference(long pageId, long referenceId);

    default Collection<TestResult> findTestResultByReference(Page page, TestHierarchy reference) {
        return findTestResultByReference(page.getId(), reference.getId());
    }
}
