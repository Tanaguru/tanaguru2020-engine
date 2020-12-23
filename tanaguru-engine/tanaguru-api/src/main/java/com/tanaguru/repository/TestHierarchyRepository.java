package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import org.aspectj.weaver.ast.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface TestHierarchyRepository extends JpaRepository<TestHierarchy, Long> {
    /**
     * Find a reference by its code
     * @param code The code
     * @return The @see TestHierarchy used for reference
     */
    Optional<TestHierarchy> findByCodeAndParentIsNull(String code);

    Optional<TestHierarchy> findByCodeAndReference(String code, TestHierarchy testHierarchy);

    Collection<TestHierarchy> findByReferenceAndTanaguruTestsContains(TestHierarchy testHierarchy, TanaguruTest tanaguruTest);

    Collection<TestHierarchy> findAllByParentIsNull();

    Optional<TestHierarchy> findByIdAndIsDeletedIsFalseAndParentIsNull(Long id);

    /**
     * Find all non deleted @see TestHierarchy
     * @return All non deleted @see TestHierarchy
     */
    Collection<TestHierarchy> findAllByParentIsNullAndIsDeletedIsFalse();
    
    Collection<TestHierarchy> findAllByReferenceId(Long reference_id);

}
