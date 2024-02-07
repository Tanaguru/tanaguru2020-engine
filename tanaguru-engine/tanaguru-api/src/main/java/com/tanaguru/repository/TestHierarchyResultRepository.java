package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Modifying
    @Transactional
    @Query("delete from TestHierarchyResult where page = :page")
    void deleteAllInBatchByPage(@Param("page") Page page);
}
