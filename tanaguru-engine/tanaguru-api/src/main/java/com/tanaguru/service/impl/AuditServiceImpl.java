package com.tanaguru.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditLog;
import com.tanaguru.domain.entity.audit.AuditReference;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.audit.parameter.AuditAuditParameterValue;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.pageresult.ElementResult;
import com.tanaguru.domain.entity.pageresult.StatusResult;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AuditAuditParameterValueRepository;
import com.tanaguru.repository.AuditLogRepository;
import com.tanaguru.repository.AuditReferenceRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.AuditSchedulerRepository;
import com.tanaguru.repository.ElementResultRepository;
import com.tanaguru.repository.StatusResultRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.repository.TestHierarchyResultRepository;
import com.tanaguru.repository.TestResultRepository;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.PageService;
import com.tanaguru.service.TestHierarchyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@Service
@Transactional
public class AuditServiceImpl implements AuditService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRepository auditRepository;
    private final AuditLogRepository auditLogRepository;
    private final ActRepository actRepository;
    private final PageService pageService;
    private final AuditReferenceRepository auditReferenceRepository;
    private final TestHierarchyService testHierarchyService;
    private final AuditAuditParameterValueRepository auditAuditParameterValueRepository;
    private final AuditSchedulerRepository auditSchedulerRepository;
    private final StatusResultRepository statusResultRepository;
    private final TestResultRepository testResultRepository;
    private final ElementResultRepository elementResultRepository;
    private final TestHierarchyRepository testHierarchyRepository;
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    
    @Autowired
    public AuditServiceImpl(
            AuditRepository auditRepository, 
            AuditLogRepository auditLogRepository, 
            ActRepository actRepository, 
            PageService pageService, 
            AuditReferenceRepository auditReferenceRepository, 
            TestHierarchyService testHierarchyService,
            AuditAuditParameterValueRepository auditAuditParameterValueRepository,
            AuditSchedulerRepository auditSchedulerRepository,
            StatusResultRepository statusResultRepository,
            TestResultRepository testResultRepository,
            ElementResultRepository elementResultRepository,
            TestHierarchyRepository testHierarchyRepository,
            TestHierarchyResultRepository testHierarchyResultRepository) {
        this.auditRepository = auditRepository;
        this.auditLogRepository = auditLogRepository;
        this.actRepository = actRepository;
        this.pageService = pageService;
        this.auditReferenceRepository = auditReferenceRepository;
        this.testHierarchyService = testHierarchyService;
        this.auditAuditParameterValueRepository = auditAuditParameterValueRepository;
        this.auditSchedulerRepository = auditSchedulerRepository;
        this.statusResultRepository = statusResultRepository;
        this.testResultRepository = testResultRepository;
        this.elementResultRepository = elementResultRepository;
        this.testHierarchyRepository = testHierarchyRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
    }

    public Collection<Audit> findAllByProject(Project project) {
        return actRepository.findAllByProject(project).stream()
                .map((Act::getAudit))
                .collect(Collectors.toList());
    }

    public void deleteAuditByProject(Project project) {
        LOGGER.debug("[Project {}] Delete all audits", project.getId());
        for (Act act : project.getActs()) {
            deleteAudit(act.getAudit());
        }
    }

    public void log(Audit audit, EAuditLogLevel level, String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAudit(audit);
        auditLog.setLevel(level);
        auditLog.setDate(new Date());
        auditLog.setMessage(message);
        auditLogRepository.save(auditLog);
    }

    public boolean canShowAudit(Audit audit, String shareCode){
        return !audit.isPrivate() || (shareCode != null && !shareCode.isEmpty() && audit.getShareCode().equals(shareCode));
    }

    public void deleteAudit(Audit audit){
        actRepository.findByAudit(audit).ifPresent(actRepository::delete);
        pageService.deletePageByAudit(audit);

        Collection<TestHierarchy> auditReferences = audit.getAuditReferences()
                .stream().map(AuditReference::getTestHierarchy).collect(Collectors.toList());
        auditRepository.deleteById(audit.getId());
        for(TestHierarchy reference : auditReferences){
            if(reference.isDeleted() && !auditReferenceRepository.existsByTestHierarchy(reference)){
                testHierarchyService.deleteReference(reference);
            }
        }
    }
    
    /**
     * Return information of the audit as a json object
     * @param audit the audit to export
     * @return json object of the audit
     */
    public JSONObject exportAudit(Audit audit) {
        JSONObject jsonFinalObject = new JSONObject();
        ObjectMapper mapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(df);
        try {
            addLogs(jsonFinalObject,audit, mapper);
            addAct(jsonFinalObject,audit, mapper);
            addScheduler(jsonFinalObject,audit, mapper);
            addParametersValues(jsonFinalObject,audit, mapper);
            addReferenceAndResults(jsonFinalObject,audit, mapper);    
        } catch (JSONException | JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonFinalObject;
    }
    
    /**
     * Add logs information of the audit to the json object
     * @param jsonFinalObject the json object
     * @param audit the audit
     * @param mapper object mapper to json
     * @throws JSONException
     * @throws JsonProcessingException
     */
    private void addLogs(JSONObject jsonFinalObject, Audit audit, ObjectMapper mapper) throws JsonProcessingException {
        Collection<AuditLog> auditLogs =  auditLogRepository.findAllByAudit(audit);
        String auditLog = "auditLogs";
        if(!auditLogs.isEmpty()) {
            JSONObject jsonAuditLogsObject = new JSONObject();
            for(AuditLog log : auditLogs) {
                jsonAuditLogsObject.append(auditLog, new JSONObject(mapper.writeValueAsString(log)));   
            }
            jsonFinalObject.put(auditLog, jsonAuditLogsObject.get(auditLog));
        }
    }
    
    /**
     * Add act information of the audit to the json object
     * @param jsonFinalObject the json object
     * @param audit the audit
     * @param mapper object mapper to json
     * @throws JSONException
     * @throws JsonProcessingException
     */
    private void addAct(JSONObject jsonFinalObject, Audit audit, ObjectMapper mapper) throws JsonProcessingException {
        Optional<Act> act = actRepository.findByAudit(audit);
        if(!act.isEmpty()) {
            JSONObject jsonActObject = new JSONObject(mapper.writeValueAsString(act.get()));
            jsonFinalObject.put("act", jsonActObject);
        }
    }
    
    /**
     * Add parameters information of the audit to the json object
     * @param jsonFinalObject the json object
     * @param audit the audit
     * @param mapper object mapper to json
     * @throws JSONException
     * @throws JsonProcessingException
     */
    private void addParametersValues(JSONObject jsonFinalObject, Audit audit, ObjectMapper mapper) throws JsonProcessingException {
        Collection<AuditAuditParameterValue> auditAuditParameterValues = auditAuditParameterValueRepository.findAllByAudit(audit);
        if(!auditAuditParameterValues.isEmpty()) {
            JSONObject jsonAuditParameterObject = new JSONObject();
            for(AuditAuditParameterValue auditParameterValue : auditAuditParameterValues) {
                jsonAuditParameterObject.append("parameters", new JSONObject(mapper.writeValueAsString(auditParameterValue.getAuditParameterValue())));
            }
            jsonFinalObject.put("auditParametersValues", jsonAuditParameterObject.get("parameters"));
        }
    }
    
    /**
     * Add scheduler information of the audit to the json object
     * @param jsonFinalObject the json object
     * @param audit the audit
     * @param mapper object mapper to json
     * @throws JsonProcessingException
     */
    private void addScheduler(JSONObject jsonFinalObject, Audit audit, ObjectMapper mapper) throws JsonProcessingException {
        Optional<AuditScheduler> auditScheduler = auditSchedulerRepository.findByAudit(audit);
        if(!auditScheduler.isEmpty()) {
            jsonFinalObject.put("auditScheduler", mapper.writeValueAsString(auditScheduler.get()));
        }
    }
    
    /**
     * Add reference and results information of the audit to the json object
     * @param jsonFinalObject the json object
     * @param audit the audit
     * @param mapper object mapper to json
     * @throws JsonProcessingException
     */
    private void addReferenceAndResults(JSONObject jsonFinalObject, Audit audit, ObjectMapper mapper) throws JsonProcessingException {
        Collection<AuditReference> auditReferences = auditReferenceRepository.findAllByAudit(audit);
        for(AuditReference ref : auditReferences) { //for each repository
            JSONObject jsonAuditReferenceObject = new JSONObject();
            jsonAuditReferenceObject.put("referenceTestHierarchyName", ref.getTestHierarchy().getName());
            jsonAuditReferenceObject.put("referenceTestHierarchyCode", ref.getTestHierarchy().getCode());
            jsonAuditReferenceObject.put("referenceTestHierarchyUrls", ref.getTestHierarchy().getUrls());
            jsonAuditReferenceObject.put("referenceTestHierarchyIsMain", ref.isMain());
            jsonAuditReferenceObject.put("referenceTestHierarchyId", ref.getTestHierarchy().getId());
            Collection<StatusResult> statusResults = statusResultRepository.findAllByReferenceAndPage_Audit(ref.getTestHierarchy(), audit);
            for(StatusResult statusResult : statusResults) { //for each page of the audit
                JSONObject jsonStatusResultsObject = new JSONObject();
                JSONObject jsonPageStatusResultsObject = new JSONObject(mapper.writeValueAsString(statusResult));
                
                Collection<TestResult> testsResults = testResultRepository.findDistinctByPageAndTanaguruTest_TestHierarchies_Reference(
                        statusResult.getPage(),
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
                //for each page of the audit add all the test hierarchy and their results (of the repository)
                addTestHierarchy(jsonPageStatusResultsObject, statusResult.getPage(), ref.getTestHierarchy().getId());
                jsonStatusResultsObject.put("pageName", statusResult.getPage().getName());
                jsonStatusResultsObject.put("pageId", statusResult.getPage().getId());
                jsonStatusResultsObject.put("pageResults", jsonPageStatusResultsObject);
                jsonAuditReferenceObject.append("auditResults", jsonStatusResultsObject);
            }
            jsonFinalObject.append("auditReference", jsonAuditReferenceObject);
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
