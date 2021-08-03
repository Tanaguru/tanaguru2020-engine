package com.tanaguru.service;

import com.tanaguru.domain.dto.AuditSynthesisDTO;
import com.tanaguru.domain.dto.TestHierarchyResultDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.data.domain.Pageable;

public interface TestHierarchyResultService {
    
    void deleteTestHierarchyResult(TestHierarchyResult testHierarchyResult);
    
    AuditSynthesisDTO getAuditSynthesisForTestHierarchy(Audit audit, TestHierarchy testHierarchy, Pageable pageable);

    TestHierarchyResultDTO getReducedResultByAudit(Audit audit, TestHierarchy testHierarchy);

    String getStatusByTestsStatus(boolean hasFailed, boolean hasSuccess, boolean hasNotApplicable, boolean hasCantTell);
    
    JSONObject toJson(TestHierarchyResult testHierarchyResult);

    void deleteTestHierarchyResultByPage(Page page);
    
    Map<String, String> getTestStatusByAuditAndTestHierarchy(Audit audit, TestHierarchy testHierarchy);
}
