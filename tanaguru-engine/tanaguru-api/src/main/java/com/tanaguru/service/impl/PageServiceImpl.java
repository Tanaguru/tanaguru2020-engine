package com.tanaguru.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.entity.audit.*;
import com.tanaguru.domain.entity.pageresult.ElementResult;
import com.tanaguru.domain.entity.pageresult.StatusResult;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.repository.*;
import com.tanaguru.service.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PageServiceImpl implements PageService {
    private final Logger LOGGER = LoggerFactory.getLogger(PageServiceImpl.class);

    private final AuditActService auditActService;
    private final AuditReferenceRepository auditReferenceRepository;
    private final ElementResultRepository elementResultRepository;
    private final PageRepository pageRepository;
    private final StatusResultRepository statusResultRepository;
    private final TanaguruTestService tanaguruTestService;
    private final TestHierarchyRepository testHierarchyRepository;
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    private final TestHierarchyResultService testHierarchyResultService;
    private final TestHierarchyService testHierarchyService;
    private final TestResultRepository testResultRepository;

    @Autowired
    public PageServiceImpl(ActRepository actRepository,
                           AuditActService auditActService,
                           AuditReferenceRepository auditReferenceRepository,
                           ElementResultRepository elementResultRepository,
                           PageRepository pageRepository,
                           StatusResultRepository statusResultRepository,
                           TanaguruTestService tanaguruTestService,
                           TestHierarchyRepository testHierarchyRepository,
                           TestHierarchyResultRepository testHierarchyResultRepository,
                           TestHierarchyResultService testHierarchyResultService,
                           TestHierarchyService testHierarchyService,
                           TestResultRepository testResultRepository) {
        this.auditActService = auditActService;
        this.auditReferenceRepository = auditReferenceRepository;
        this.elementResultRepository = elementResultRepository;
        this.pageRepository = pageRepository;
        this.statusResultRepository = statusResultRepository;
        this.tanaguruTestService = tanaguruTestService;
        this.testHierarchyRepository = testHierarchyRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.testHierarchyResultService = testHierarchyResultService;
        this.testHierarchyService = testHierarchyService;
        this.testResultRepository = testResultRepository;
    }

    @Override
    public void deletePage(Page page) {
        LOGGER.info("[Page {}] delete", page.getId());
        pageRepository.delete(page);
    }

    @Override
    public void deletePageByAudit(Audit audit) {
        LOGGER.info("[Audit {}] Delete pages", audit.getId());
        for (Page page : pageRepository.findAllByAudit_Id(audit.getId())) {
            deletePage(page);
        }

    }

    /**
     * Return a json object with the information of the page and the audit
     *
     * @param page
     * @return json object
     */
    public JSONObject toJsonWithAuditInfo(Page page) {
        JSONObject jsonPageObject = new JSONObject();
        jsonPageObject.put("pageName", page.getName());
        jsonPageObject.put("pageUrl", page.getUrl());
        jsonPageObject.put("pageId", page.getId());
        jsonPageObject.put("pageRank", page.getRank());
        jsonPageObject.put("act", auditActService.toJson(page.getAudit())); //audit info
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        try {
            addReferencesAndResults(page, jsonPageObject, mapper);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error in serializing page to json");
        }
        return jsonPageObject;
    }

    /**
     * Return a json object with the information of the page
     *
     * @param page
     * @return json object
     */
    public JSONObject toJson(Page page) {
        JSONObject jsonPageObject = new JSONObject();
        jsonPageObject.put("pageName", page.getName());
        jsonPageObject.put("pageUrl", page.getUrl());
        jsonPageObject.put("pageId", page.getId());
        jsonPageObject.put("pageRank", page.getRank());
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        try {
            addReferencesAndResults(page, jsonPageObject, mapper);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error in serializing page to json");
        }
        return jsonPageObject;
    }

    /**
     * Add reference and results information of the page to the json object
     *
     * @param page            the page
     * @param jsonFinalObject the json object
     * @param mapper
     * @throws JsonProcessingException
     */
    private void addReferencesAndResults(Page page, JSONObject jsonFinalObject, ObjectMapper mapper) throws JsonProcessingException {
        Collection<AuditReference> auditReferences = auditReferenceRepository.findAllByAudit(page.getAudit());
        for (AuditReference ref : auditReferences) { //for each repository
            JSONObject referenceTestHierarchy = testHierarchyService.toJson(ref.getTestHierarchy());
            referenceTestHierarchy.put("referenceTestHierarchyIsMain", ref.isMain());
            Optional<StatusResult> statusResult = statusResultRepository.findByReferenceAndPage(ref.getTestHierarchy(), page);
            if (statusResult.isPresent()) {
                StatusResult statusRes = statusResult.get();
                JSONObject jsonStatusResultsObject = new JSONObject();
                JSONObject jsonPageStatusResultsObject = new JSONObject(mapper.writeValueAsString(statusRes));

                Collection<TestResult> testsResults = testResultRepository.findTestResultByReference(
                        statusRes.getPage(),
                        ref.getTestHierarchy());
                for (TestResult testResult : testsResults) { //for each test
                    TanaguruTest tanaguruTest = testResult.getTanaguruTest();
                    JSONObject oneTestResult = new JSONObject(mapper.writeValueAsString(testResult));
                    JSONArray elementResults = oneTestResult.getJSONArray("elementResults");
                    List<Long> longs = elementResults.toList().stream()
                            .map(object -> Long.parseLong(String.valueOf(object)))
                            .collect(Collectors.toList());
                    Collection<ElementResult> elements = elementResultRepository.findAllByIdIn(longs);
                    oneTestResult.put("elementResults", new JSONArray(mapper.writeValueAsString(elements)));

                    JSONObject tanaguruTestJson = tanaguruTestService.toJson(tanaguruTest);
                    for (TestHierarchy th : tanaguruTest.getTestHierarchies()) { //for each test hierarchy corresponding to the tanaguru test
                        JSONObject testHierarchyInfos = testHierarchyService.toJson(th);
                        tanaguruTestJson.append("testHierarchy", testHierarchyInfos);
                    }
                    oneTestResult.put("tanaguruTest", tanaguruTestJson);
                    jsonPageStatusResultsObject.append("testsResults", oneTestResult);
                }
                //add all the test hierarchy and their results (of the repository)
                addTestHierarchy(jsonPageStatusResultsObject, statusRes.getPage(), ref.getTestHierarchy().getId());
                jsonStatusResultsObject.put("pageResults", jsonPageStatusResultsObject);
                jsonStatusResultsObject.put("reference", referenceTestHierarchy);
                jsonFinalObject.append("auditReference", jsonStatusResultsObject);
            }
        }
    }

    /**
     * Add all test hierarchy results of the page to the json object
     *
     * @param pageResultObject         intermediate json object (page results)
     * @param page                     the page
     * @param referenceTestHierarchyId the reference test hierarchy id
     */
    private void addTestHierarchy(JSONObject pageResultObject, Page page, long referenceTestHierarchyId) {
        Collection<TestHierarchy> testHierarchies = testHierarchyRepository.findAllByReferenceId(referenceTestHierarchyId);
        for (TestHierarchy th : testHierarchies) { //for each test hierarchy of the repository
            Optional<TestHierarchyResult> testHierarchyResult = testHierarchyResultRepository.findByTestHierarchyAndPage(th, page);
            if (testHierarchyResult.isPresent()) {
                TestHierarchyResult thr = testHierarchyResult.get();
                JSONObject testHierarchy = testHierarchyService.toJson(th);
                long[] tanaguruTestIds = new long[th.getTanaguruTests().size()];
                int i = 0;
                for (TanaguruTest tanaguruTest : th.getTanaguruTests()) {
                    tanaguruTestIds[i] = tanaguruTest.getId();
                    i++;
                }
                testHierarchy.put("tanaguruTestId", tanaguruTestIds);
                testHierarchy.put("testHierarchyResult", testHierarchyResultService.toJson(thr));
                pageResultObject.append("testHierarchy", testHierarchy);
            }
        }
    }
}
