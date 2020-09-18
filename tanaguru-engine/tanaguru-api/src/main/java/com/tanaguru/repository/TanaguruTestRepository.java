package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author rcharre
 */
@Repository
public interface TanaguruTestRepository extends JpaRepository<TanaguruTest, Long> {
    /**
     * Find all non deleted tests for a collection of reference
     * @param references the Collection of references
     * @return all non deleted tests for the collection
     */
    Collection<TanaguruTest> findDistinctByTestHierarchies_ReferenceInAndIsDeletedIsFalse(Collection<TestHierarchy> references);

    /**
     * Find all tests for a collection of reference
     * @param references the Collection of references
     * @return all tests for the collection
     */
    Collection<TanaguruTest> findDistinctByTestHierarchies_ReferenceIn(Collection<TestHierarchy> references);

    /**
     * Find all tests of a reference
     * @param reference the reference
     * @return all tests for a reference
     */
    Collection<TanaguruTest> findAllByTestHierarchies_Reference(TestHierarchy reference);

    /**
     * Find all non deleted tests of a reference
     * @param reference the reference
     * @return all non deleted tests for a reference
     */
    Collection<TanaguruTest> findAllByTestHierarchies_ReferenceAndIsDeletedIsFalse(TestHierarchy reference);

    /**
     * Find all tests of a testHierarchy
     * @param testHierarchy the testHierarchy
     * @return all tests for a testHierarchy
     */
    Collection<TanaguruTest> findAllByTestHierarchiesContains(TestHierarchy testHierarchy);

    /**
     * Find all non deleted tests of a testHierarchy
     * @param testHierarchy the testHierarchy
     * @return all non deleted tests for a testHierarchy
     */
    Collection<TanaguruTest> findAllByTestHierarchiesContainsAndIsDeletedIsFalse(TestHierarchy testHierarchy);
}
