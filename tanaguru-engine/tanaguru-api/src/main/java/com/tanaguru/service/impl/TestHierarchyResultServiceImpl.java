package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.TestStatusName;
import com.tanaguru.domain.dto.AuditSynthesisDTO;
import com.tanaguru.domain.dto.TestHierarchyDTO;
import com.tanaguru.domain.dto.TestHierarchyResultDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.repository.TestHierarchyResultRepository;
import com.tanaguru.service.TestHierarchyResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestHierarchyResultServiceImpl implements TestHierarchyResultService {
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    private final PageRepository pageRepository;

    @Autowired
    public TestHierarchyResultServiceImpl(TestHierarchyResultRepository testHierarchyResultRepository, PageRepository pageRepository) {
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.pageRepository = pageRepository;
    }

    @Override
    public void deleteTestHierarchyResult(TestHierarchyResult testHierarchyResult) {
        testHierarchyResultRepository.delete(testHierarchyResult);
    }

    public AuditSynthesisDTO getAuditSynthesisForTestHierarchy(Audit audit, TestHierarchy testHierarchy, Pageable pageable ){
        Map<String, Map<Long, TestHierarchyResultDTO>> result = new LinkedHashMap<>();
        org.springframework.data.domain.Page<Page> pages = pageRepository.findAllByAudit_Id(audit.getId(), pageable);

        getAuditSynthesisForTestHierarchyRec(result, pages.getContent(), testHierarchy);
        return new AuditSynthesisDTO(result, pages.getTotalElements(), pages.getTotalPages(), pageable);

    }

    private Map<String, Map<Long, TestHierarchyResultDTO>> getAuditSynthesisForTestHierarchyRec(
            Map<String, Map<Long, TestHierarchyResultDTO>> result,
            Collection<Page> pages,
            TestHierarchy testHierarchy){
        if(testHierarchy.getChildren().isEmpty()){
            Map<Long, TestHierarchyResultDTO> testHierarchyResultMap = new LinkedHashMap<>();
            testHierarchyResultRepository.findAllByPage_InAndTestHierarchy(pages, testHierarchy)
                    .forEach(testHierarchyResult -> {
                testHierarchyResultMap.put(testHierarchyResult.getPage().getId(), new TestHierarchyResultDTO(testHierarchyResult));
            });
            result.put(testHierarchy.getCode(), testHierarchyResultMap);
        }else{
            testHierarchy.setChildren(
                    testHierarchy.getChildren().stream().sorted(Comparator.comparingInt(TestHierarchy::getRank)).collect(Collectors.toList())
            );
            for(TestHierarchy child : testHierarchy.getChildren()){
                getAuditSynthesisForTestHierarchyRec(result, pages, child);
            }
        }
        return result;
    }

    public TestHierarchyResultDTO getReducedResultByAudit(Audit audit, TestHierarchy testHierarchy){
        Collection<TestHierarchyResult> auditTestHierarchyResultArray = testHierarchyResultRepository.findAllByPage_AuditAndTestHierarchy(audit, testHierarchy);
        TestHierarchyResultDTO reducedResult = new TestHierarchyResultDTO();
        for(TestHierarchyResult testHierarchyResult : auditTestHierarchyResultArray){
            reducedResult.nbF += testHierarchyResult.getNbFailed();
            reducedResult.nbP += testHierarchyResult.getNbPassed();
            reducedResult.nbI += testHierarchyResult.getNbInapplicable();
            reducedResult.nbU += testHierarchyResult.getNbUntested();
            reducedResult.nbCT += testHierarchyResult.getNbCantTell();
            reducedResult.nbTF += testHierarchyResult.getNbTestFailed();
            reducedResult.nbTP += testHierarchyResult.getNbTestPassed();
            reducedResult.nbTI += testHierarchyResult.getNbTestInapplicable();
            reducedResult.nbTCT += testHierarchyResult.getNbTestCantTell();
            reducedResult.nbECT += testHierarchyResult.getNbElementCantTell();
            reducedResult.nbEF += testHierarchyResult.getNbElementFailed();
            reducedResult.nbEP += testHierarchyResult.getNbElementPassed();
            reducedResult.nbET += testHierarchyResult.getNbElementTested();
            reducedResult.testHierarchy = new TestHierarchyDTO(testHierarchyResult.getTestHierarchy());
            reducedResult.status = getStatusByTestsStatus(
                    reducedResult.nbF != 0,
                    reducedResult.nbP != 0,
                    reducedResult.nbI != 0,
                    reducedResult.nbCT != 0
            );
        }
        return reducedResult;
    }

    public String getStatusByTestsStatus(boolean hasFailed, boolean hasSuccess, boolean hasNotApplicable, boolean hasCantTell){
        if(hasFailed){
            return TestStatusName.STATUS_FAILED;
        }else if (hasCantTell){
            return TestStatusName.STATUS_CANT_TELL;
        }else if(hasSuccess){
            return TestStatusName.STATUS_SUCCESS;
        }else if(hasNotApplicable){
            return TestStatusName.STATUS_INAPPLICABLE;
        }else{
            return TestStatusName.STATUS_NOT_TESTED;
        }
    }
}
