package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.TestStatusName;
import com.tanaguru.domain.entity.audit.*;
import com.tanaguru.domain.entity.pageresult.ElementResult;
import com.tanaguru.domain.entity.pageresult.StatusResult;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.repository.*;
import com.tanaguru.service.ResultAnalyzerService;
import com.tanaguru.service.TestHierarchyResultService;
import com.tanaguru.webextresult.WebextPageResult;
import com.tanaguru.webextresult.WebextTestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ResultAnalyzerServiceImpl implements ResultAnalyzerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultAnalyzerServiceImpl.class);
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    private final ElementResultRepository elementResultRepository;
    private final TestResultRepository testResultRepository;
    private final TanaguruTestRepository tanaguruTestRepository;
    private final StatusResultRepository statusResultRepository;
    private final TestHierarchyResultService testgetStatusByTestsStatus;


    @Autowired
    public ResultAnalyzerServiceImpl(
            TestHierarchyResultRepository testHierarchyResultRepository,
            AuditReferenceRepository auditReferenceRepository,
            ElementResultRepository elementResultRepository,
            TestResultRepository testResultRepository, TanaguruTestRepository tanaguruTestRepository, StatusResultRepository statusResultRepository, TestHierarchyResultService testgetStatusByTestsStatus) {
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.elementResultRepository = elementResultRepository;
        this.testResultRepository = testResultRepository;
        this.tanaguruTestRepository = tanaguruTestRepository;
        this.statusResultRepository = statusResultRepository;
        this.testgetStatusByTestsStatus = testgetStatusByTestsStatus;
    }

    public void extractWebextPageResult(WebextPageResult webextPageResult, Audit audit, Page page) {
        LOGGER.info("[Audit {}] extract result for page {}", audit.getId(), page.getId());
        Collection<AuditReference> auditReferences = auditReferenceRepository.findAllByAudit(audit);
        Map<Long, TestResult> testResultByTestId = extractWebextTestResult(webextPageResult.getTests(), page);

        Map<Long, StatusResult> statusResultByReferenceId = new HashMap<>();
        for (TestResult testResult : testResultByTestId.values()) {
            for (AuditReference auditReference : auditReferences) {
                if (testResult.getTanaguruTest().getTestHierarchies().stream().anyMatch(testHierarchy ->
                        testHierarchy.getReference().getId() == auditReference.getTestHierarchy().getId()
                )) {
                    StatusResult statusResult;
                    if (!statusResultByReferenceId.containsKey(auditReference.getTestHierarchy().getId())) {
                        statusResult = new StatusResult();
                        statusResult.setReference(auditReference.getTestHierarchy());
                        statusResult.setPage(page);
                        statusResultByReferenceId.put(auditReference.getTestHierarchy().getId(), statusResult);
                    } else {
                        statusResult = statusResultByReferenceId.get(auditReference.getTestHierarchy().getId());
                    }

                    statusResult.setNbElementCantTell(statusResult.getNbElementCantTell() + testResult.getNbElementCantTell());
                    statusResult.setNbElementFailed(statusResult.getNbElementFailed() + testResult.getNbElementFailed());
                    statusResult.setNbElementPassed(statusResult.getNbElementPassed() + testResult.getNbElementPassed());
                    statusResult.setNbElementTested(statusResult.getNbElementTested() + testResult.getNbElementTested());
                    statusResult.setNbElementUntested(statusResult.getNbElementUntested() + testResult.getNbElementUntested());
                    switch (testResult.getStatus()) {
                        case TestStatusName.STATUS_FAILED:
                            statusResult.setNbTestFailed(statusResult.getNbTestFailed() + 1);
                            break;
                        case TestStatusName.STATUS_SUCCESS:
                            statusResult.setNbTestPassed(statusResult.getNbTestPassed() + 1);
                            break;
                        case TestStatusName.STATUS_INAPPLICABLE:
                            statusResult.setNbTestInapplicable(statusResult.getNbTestInapplicable() + 1);
                            break;
                        case TestStatusName.STATUS_CANT_TELL:
                            statusResult.setNbTestCantTell(statusResult.getNbTestCantTell() + 1);
                            break;
                        case TestStatusName.STATUS_NOT_TESTED:
                            statusResult.setNbTestUntested(statusResult.getNbTestUntested() + 1);
                            break;
                        default:
                    }
                }
            }

            statusResultByReferenceId.values()
                    .forEach(statusResultRepository::save);
        }

        auditReferences.stream()
                .map(AuditReference::getTestHierarchy)
                .forEach(testHierarchy -> extractWebextTestsResultByTestHierarchy(
                        testResultByTestId,
                        testHierarchy,
                        page,
                        null));
    }

    public Map<Long, TestResult> extractWebextTestResult(Collection<WebextTestResult> webextTestResults, Page page) {
        Map<Long, TestResult> testResultByTestId = new HashMap<>();
        for (WebextTestResult webextTestResult : webextTestResults) {
            TestResult testResult = new TestResult();
            testResult.setPage(page);
            testResult.setMarks(webextTestResult.getMarks());
            testResult.setTanaguruTest(tanaguruTestRepository.getOne(webextTestResult.getId()));
            testResult.setNbElementTested(webextTestResult.getCounter());
            testResult.setStatus(webextTestResult.getType());
            testResult = testResultRepository.save(testResult);

            Collection<ElementResult> elementResults = new ArrayList<>();
            for (ElementResult elementResult : webextTestResult.getData()) {
                elementResult.setTestResult(testResult);
                switch (elementResult.getStatus()) {
                    case TestStatusName.STATUS_FAILED:
                        testResult.setNbElementFailed(testResult.getNbElementFailed() + 1);
                        break;

                    case TestStatusName.STATUS_SUCCESS:
                        testResult.setNbElementPassed(testResult.getNbElementPassed() + 1);
                        break;

                    case TestStatusName.STATUS_CANT_TELL:
                        testResult.setNbElementCantTell(testResult.getNbElementCantTell() + 1);
                        break;
                    case TestStatusName.STATUS_NOT_TESTED:
                        testResult.setNbElementUntested(testResult.getNbElementUntested() + 1);
                        break;
                    default:
                }
                elementResults.add(elementResultRepository.save(elementResult));
            }
            testResult.setElementResults(elementResults);
            testResultByTestId.put(webextTestResult.getId(), testResultRepository.save(testResult));
        }

        return testResultByTestId;
    }

    public TestHierarchyResult extractWebextTestsResultByTestHierarchy(Map<Long, TestResult> testResultByTestId,
                                                                       TestHierarchy testHierarchy,
                                                                       Page page,
                                                                       TestHierarchyResult parent) {
        TestHierarchyResult testHierarchyResult = new TestHierarchyResult();
        testHierarchyResult.setTestHierarchy(testHierarchy);
        testHierarchyResult.setPage(page);
        testHierarchyResult.setParent(parent);
        testHierarchyResult = testHierarchyResultRepository.save(testHierarchyResult);

        Collection<TestResult> testResults = new ArrayList<>();
        for (TanaguruTest tanaguruTest : testHierarchy.getTanaguruTests()) {
            TestResult testResult = testResultByTestId.get(tanaguruTest.getId());
            switch (testResult.getStatus()) {
                case TestStatusName.STATUS_FAILED:
                    testHierarchyResult.setNbTestFailed(testHierarchyResult.getNbTestFailed() + 1);
                    break;
                case TestStatusName.STATUS_SUCCESS:
                    testHierarchyResult.setNbTestPassed(testHierarchyResult.getNbTestPassed() + 1);
                    break;
                case TestStatusName.STATUS_INAPPLICABLE:
                    testHierarchyResult.setNbTestInapplicable(testHierarchyResult.getNbTestInapplicable() + 1);
                    break;
                case TestStatusName.STATUS_CANT_TELL:
                    testHierarchyResult.setNbTestCantTell(testHierarchyResult.getNbTestCantTell() + 1);
                    break;
                case TestStatusName.STATUS_NOT_TESTED:
                    testHierarchyResult.setNbTestUntested(testHierarchyResult.getNbTestUntested() + 1);
                    break;
                default:
            }
            testResults.add(testResult);
            testHierarchyResult.setNbElementTested(testHierarchyResult.getNbElementTested() + testResult.getNbElementTested());
            testHierarchyResult.setNbElementCantTell(testHierarchyResult.getNbElementCantTell() + testResult.getNbElementCantTell());
            testHierarchyResult.setNbElementFailed(testHierarchyResult.getNbElementFailed() + testResult.getNbElementFailed());
            testHierarchyResult.setNbElementPassed(testHierarchyResult.getNbElementPassed() + testResult.getNbElementPassed());
            testHierarchyResult.setNbElementUntested(testHierarchyResult.getNbElementUntested() + testResult.getNbElementUntested());
        }
        testHierarchyResult.setTestResults(testResults);
        if (testHierarchy.getChildren().isEmpty()) {
            testHierarchyResult.setStatus(testgetStatusByTestsStatus.getStatusByTestsStatus(
                    testHierarchyResult.getNbTestFailed() != 0,
                    testHierarchyResult.getNbTestPassed() != 0,
                    testHierarchyResult.getNbTestInapplicable() != 0,
                    testHierarchyResult.getNbTestCantTell() != 0
            ));
            switch (testHierarchyResult.getStatus()) {
                case TestStatusName.STATUS_FAILED:
                    testHierarchyResult.setNbFailed(1);
                    break;
                case TestStatusName.STATUS_SUCCESS:
                    testHierarchyResult.setNbPassed(1);
                    break;
                case TestStatusName.STATUS_NOT_TESTED:
                    testHierarchyResult.setNbUntested(1);
                    break;
                case TestStatusName.STATUS_INAPPLICABLE:
                    testHierarchyResult.setNbInapplicable(1);
                    break;
                case TestStatusName.STATUS_CANT_TELL:
                    testHierarchyResult.setNbCantTell(1);
                    break;
                default:
            }
        } else {
            for (TestHierarchy child : testHierarchy.getChildren()) {
                TestHierarchyResult childResult = extractWebextTestsResultByTestHierarchy(testResultByTestId, child, page, testHierarchyResult);
                testHierarchyResult.setNbCantTell(testHierarchyResult.getNbCantTell() + childResult.getNbCantTell());
                testHierarchyResult.setNbUntested(testHierarchyResult.getNbUntested() + childResult.getNbUntested());
                testHierarchyResult.setNbFailed(testHierarchyResult.getNbFailed() + childResult.getNbFailed());
                testHierarchyResult.setNbPassed(testHierarchyResult.getNbPassed() + childResult.getNbPassed());
                testHierarchyResult.setNbInapplicable(testHierarchyResult.getNbInapplicable() + childResult.getNbInapplicable());
                testHierarchyResult.setNbTestCantTell(testHierarchyResult.getNbTestCantTell() + childResult.getNbTestCantTell());
                testHierarchyResult.setNbTestFailed(testHierarchyResult.getNbTestFailed() + childResult.getNbTestFailed());
                testHierarchyResult.setNbTestPassed(testHierarchyResult.getNbTestPassed() + childResult.getNbTestPassed());
                testHierarchyResult.setNbTestInapplicable(testHierarchyResult.getNbTestInapplicable() + childResult.getNbTestInapplicable());
                testHierarchyResult.setNbElementTested(testHierarchyResult.getNbElementTested() + childResult.getNbElementTested());
                testHierarchyResult.setNbElementFailed(testHierarchyResult.getNbElementFailed() + childResult.getNbElementFailed());
                testHierarchyResult.setNbElementPassed(testHierarchyResult.getNbElementPassed() + childResult.getNbElementPassed());
                testHierarchyResult.setNbElementCantTell(testHierarchyResult.getNbElementCantTell() + childResult.getNbElementCantTell());
                testHierarchyResult.setNbElementUntested(testHierarchyResult.getNbElementUntested() + childResult.getNbElementUntested());
            }
            testHierarchyResult.setStatus(testgetStatusByTestsStatus.getStatusByTestsStatus(
                    testHierarchyResult.getNbFailed() != 0,
                    testHierarchyResult.getNbPassed() != 0,
                    testHierarchyResult.getNbInapplicable() != 0,
                    testHierarchyResult.getNbCantTell() != 0
            ));
        }

        return testHierarchyResultRepository.save(testHierarchyResult);
    }
}
