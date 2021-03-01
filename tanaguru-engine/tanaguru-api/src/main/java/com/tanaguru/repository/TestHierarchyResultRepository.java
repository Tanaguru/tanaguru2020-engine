package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface TestHierarchyResultRepository extends JpaRepository<TestHierarchyResult, Long> {
    Optional<TestHierarchyResult> findByTestHierarchyAndPage(TestHierarchy testHierarchy, Page page);
    Collection<TestHierarchyResult> findAllByPage(Page page);
    Collection<TestHierarchyResult> findAllByPage_InAndTestHierarchy(Collection<Page> pages, TestHierarchy testHierarchy);
    Collection<TestHierarchyResult> findAllByPageAndTestHierarchy(Page page, TestHierarchy testHierarchy);
    Collection<TestHierarchyResult> findAllByPage_AuditAndTestHierarchy(Audit audit, TestHierarchy testHierarchy);
}
