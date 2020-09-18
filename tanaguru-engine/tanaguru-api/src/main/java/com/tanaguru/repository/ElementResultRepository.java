package com.tanaguru.repository;

import com.tanaguru.domain.entity.pageresult.ElementResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ElementResultRepository extends JpaRepository<ElementResult, Long> {
    Collection<ElementResult> findAllByIdIn(Collection<Long> ids);
    Page<ElementResult> findAllByTestResult(TestResult testResult, Pageable pageable);
}
