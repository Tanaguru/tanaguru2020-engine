package com.tanaguru.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.pageresult.ElementResult;
import com.tanaguru.domain.entity.pageresult.StatusResult;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AuditReferenceRepository;
import com.tanaguru.repository.ElementResultRepository;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.repository.StatusResultRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.repository.TestHierarchyResultRepository;
import com.tanaguru.repository.TestResultRepository;
import com.tanaguru.service.PageService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
@Transactional
public class PageServiceImpl implements PageService {
    
    private final PageRepository pageRepository;
    private final ActRepository actRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    private final StatusResultRepository statusResultRepository;
    private final TestResultRepository testResultRepository;
    private final ElementResultRepository elementResultRepository;
    private final TestHierarchyRepository testHierarchyRepository;
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    
    @Autowired
    public PageServiceImpl(PageRepository pageRepository,
            ActRepository actRepository,
            AuditReferenceRepository auditReferenceRepository,
            StatusResultRepository statusResultRepository,
            TestResultRepository testResultRepository,
            ElementResultRepository elementResultRepository,
            TestHierarchyRepository testHierarchyRepository,
            TestHierarchyResultRepository testHierarchyResultRepository) {
        this.pageRepository = pageRepository;
        this.actRepository = actRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.statusResultRepository = statusResultRepository;
        this.testResultRepository = testResultRepository;
        this.elementResultRepository = elementResultRepository;
        this.testHierarchyRepository = testHierarchyRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
    }

    @Override
    public void deletePage(Page page) {
        pageRepository.delete(page);
    }

    @Override
    public void deletePageByAudit(Audit audit) {
        for(Page page : audit.getPages()){
            deletePage(page);
        }
    }
    
    /**
     * Return information of the page as a json object
     * @param page the page to export
     * @return json object of the page
     */
    public JSONObject exportPage(Page page) {
        JSONObject jsonFinalObject = new JSONObject();
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        try {
            addPageInformation(page, jsonFinalObject, mapper);
            addReferencesAndResults(page, jsonFinalObject, mapper);
        } catch (JSONException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonFinalObject;
    }
    
    /**
     * Add general information of the page to the json object
     * @param page the page
     * @param jsonFinalObject the json object
     * @param mapper
     * @throws JsonProcessingException
     */
    private void addPageInformation(Page page, JSONObject jsonFinalObject, ObjectMapper mapper) throws JsonProcessingException {
        JSONObject pageInformationJson = new JSONObject();
        pageInformationJson.put("pageName", page.getName());
        pageInformationJson.put("pageUrl", page.getUrl());
        pageInformationJson.put("pageId", page.getId());
        pageInformationJson.put("pageRank", page.getRank());
        Optional<Act> act = actRepository.findByAudit(page.getAudit());
        if(!act.isEmpty()) {
            JSONObject jsonAuditObject = new JSONObject(mapper.writeValueAsString(act.get()));
            pageInformationJson.put("act", jsonAuditObject);
        }
        jsonFinalObject.put("pageInformation", pageInformationJson);
    }
    
    /**
     * Add reference and results information of the page to the json object
     * @param page the page
     * @param jsonFinalObject the json object
     * @param mapper
     * @throws JsonProcessingException 
     */
    private void addReferencesAndResults(Page page, JSONObject jsonFinalObject, ObjectMapper mapper) throws JsonProcessingException {
        Collection<AuditReference> auditReferences = auditReferenceRepository.findAllByAudit(page.getAudit());
        for(AuditReference ref : auditReferences) { //for each repository
            JSONObject jsonAuditReferenceObject = new JSONObject();
            jsonAuditReferenceObject.put("referenceTestHierarchyName", ref.getTestHierarchy().getName());
            jsonAuditReferenceObject.put("referenceTestHierarchyCode", ref.getTestHierarchy().getCode());
            jsonAuditReferenceObject.put("referenceTestHierarchyUrls", ref.getTestHierarchy().getUrls());
            jsonAuditReferenceObject.put("referenceTestHierarchyIsMain", ref.isMain());
            jsonAuditReferenceObject.put("referenceTestHierarchyId", ref.getTestHierarchy().getId());
            Optional<StatusResult> statusResult = statusResultRepository.findByReferenceAndPage(ref.getTestHierarchy(), page);
            if(!statusResult.isEmpty()) {
                StatusResult statusRes = statusResult.get();
                JSONObject jsonStatusResultsObject = new JSONObject();
                JSONObject jsonPageStatusResultsObject = new JSONObject(mapper.writeValueAsString(statusRes));
                
                Collection<TestResult> testsResults = testResultRepository.findDistinctByPageAndTanaguruTest_TestHierarchies_Reference(
                        statusRes.getPage(),
                        ref.getTestHierarchy());
                for(TestResult testResult : testsResults) { //for each test
                    TanaguruTest tanaguruTest = testResult.getTanaguruTest();                       
                    JSONObject oneTestResult = new JSONObject(mapper.writeValueAsString(testResult));
                    JSONArray elementResults = oneTestResult.getJSONArray("elementResults");
                    List<Long> longs = elementResults.toList().stream()
                            .map(object -> Long.parseLong(String.valueOf(object)))
                            .collect(Collectors.toList());
                    Collection<ElementResult> elements = elementResultRepository.findAllByIdIn(longs);
                    oneTestResult.put("elementResults", new JSONArray(mapper.writeValueAsString(elements)));
    
                    JSONObject tanaguruTestJson = new JSONObject();
                    tanaguruTestJson.put("name", tanaguruTest.getName());
                    tanaguruTestJson.put("description", tanaguruTest.getDescription());
                    tanaguruTestJson.put("id", tanaguruTest.getId());
                    tanaguruTestJson.put("tags", tanaguruTest.getTags());
                    for(TestHierarchy th : tanaguruTest.getTestHierarchies()) { //for each test hierarchy corresponding to the tanaguru test
                        JSONObject testHierarchyInfos = new JSONObject();
                        testHierarchyInfos.put("name", th.getName());
                        testHierarchyInfos.put("code", th.getCode());
                        testHierarchyInfos.put("id", th.getId());
                        testHierarchyInfos.put("urls", th.getUrls());
                        tanaguruTestJson.append("testHierarchy", testHierarchyInfos);
                    }
                    oneTestResult.put("tanaguruTest", tanaguruTestJson);
                    jsonPageStatusResultsObject.append("testsResults", oneTestResult);
                }
                //add all the test hierarchy and their results (of the repository)
                addTestHierarchy(jsonPageStatusResultsObject, statusRes.getPage(), ref.getTestHierarchy().getId());
                jsonStatusResultsObject.put("pageName", statusRes.getPage().getName());
                jsonStatusResultsObject.put("pageId", statusRes.getPage().getId());
                jsonStatusResultsObject.put("pageResults", jsonPageStatusResultsObject);
                jsonStatusResultsObject.put("reference", jsonAuditReferenceObject);
                jsonFinalObject.append("auditReference", jsonStatusResultsObject);
            }
        }
    }
    
    /**
     * Add test hierarchy results of the page to the json object
     * @param pageResultObject intermediate json object (page results)
     * @param page the page
     * @param referenceTestHierarchyId the reference test hierarchy id
     */
    public void addTestHierarchy(JSONObject pageResultObject, Page page, long referenceTestHierarchyId) {
        Collection<TestHierarchy> testHierarchies = testHierarchyRepository.findAllByReferenceId(referenceTestHierarchyId);
        for(TestHierarchy th : testHierarchies) { //for each test hierarchy of the repository
            Optional<TestHierarchyResult> testHierarchyResult = testHierarchyResultRepository.findByTestHierarchyAndPage(th, page);
            if(!testHierarchyResult.isEmpty()) {
                TestHierarchyResult thr = testHierarchyResult.get();
                JSONObject testHierarchy = new JSONObject();
                testHierarchy.put("testHierarchyName", th.getName());
                testHierarchy.put("testHierarchyCode", th.getCode());
                testHierarchy.put("testHierarchyUrl", th.getUrls());
                testHierarchy.put("testHierarchyId", th.getId());
                long[] tanaguruTestIds = new long[th.getTanaguruTests().size()];
                int i=0;
                for(TanaguruTest tanaguruTest : th.getTanaguruTests()) {
                    tanaguruTestIds[i] = tanaguruTest.getId();
                    i++;
                }
                testHierarchy.put("tanaguruTestId", tanaguruTestIds);
                
                JSONObject results = new JSONObject();
                results.put("nb_element_cant_tell", thr.getNbElementCantTell());
                results.put("nb_cant_tell", thr.getNbCantTell());
                results.put("nb_element_failed", thr.getNbElementFailed());
                results.put("nb_element_passed", thr.getNbElementPassed());
                results.put("nb_element_tested", thr.getNbElementTested());
                results.put("nb_element_untested", thr.getNbElementUntested());
                results.put("nb_failed", thr.getNbFailed());
                results.put("nb_inapplicable", thr.getNbInapplicable());
                results.put("nb_passed", thr.getNbPassed());
                results.put("nb_test_cant_tell", thr.getNbTestCantTell());
                results.put("nb_test_failed", thr.getNbTestFailed());
                results.put("nb_test_inapplicable", thr.getNbTestInapplicable());
                results.put("nb_test_passed", thr.getNbTestPassed());
                results.put("nb_test_untested", thr.getNbTestUntested());
                results.put("nb_untested", thr.getNbUntested());
                results.put("status", thr.getStatus());
                

                testHierarchy.put("testHierarchyResult",results);
                pageResultObject.append("testHierarchy",testHierarchy);
            }       
        }
    }
}
