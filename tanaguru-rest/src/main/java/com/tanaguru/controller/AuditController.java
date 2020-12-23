package com.tanaguru.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.dto.AuditCommandDTO;
import com.tanaguru.domain.dto.DemoCommandDTO;
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
import com.tanaguru.domain.exception.ForbiddenException;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.repository.*;
import com.tanaguru.service.*;

import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ByteArrayResource;
import javax.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import javax.validation.Valid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/audits")
public class AuditController {

    private final AuditRepository auditRepository;
    private final AuditService auditService;
    private final AuditFactory auditFactory;
    private final AuditRunnerService auditRunnerService;
    private final ProjectRepository projectRepository;
    private final ActRepository actRepository;
    private final TestHierarchyRepository testHierarchyRepository;
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    private final AuditAuditParameterValueRepository auditAuditParameterValueRepository;
    private final AuditSchedulerRepository auditSchedulerRepository;
    private final StatusResultRepository statusResultRepository;
    private final TestResultRepository testResultRepository;
    private final ElementResultRepository elementResultRepository;
    private final PageRepository pageRepository;
    
    @Autowired
    public AuditController(
            AuditRepository auditRepository,
            AuditService auditService, AuditFactory auditFactory,
            AuditRunnerService auditRunnerService,
            ProjectRepository projectRepository,
            ActRepository actRepository,
            TestHierarchyRepository testHierarchyRepository,
            AuditLogRepository auditLogRepository,
            AuditReferenceRepository auditReferenceRepository,
            AuditAuditParameterValueRepository auditAuditParameterValueRepository,
            AuditSchedulerRepository auditSchedulerRepository,
            StatusResultRepository statusResultRepository,
            TestResultRepository testResultRepository,
            ElementResultRepository elementResultRepository,
            TestHierarchyResultRepository testHierarchyResultRepository,
            PageRepository pageRepository) {

        this.auditRepository = auditRepository;
        this.auditService = auditService;
        this.auditFactory = auditFactory;
        this.auditRunnerService = auditRunnerService;
        this.projectRepository = projectRepository;
        this.actRepository = actRepository;
        this.testHierarchyRepository = testHierarchyRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.auditAuditParameterValueRepository = auditAuditParameterValueRepository;
        this.auditSchedulerRepository = auditSchedulerRepository;
        this.statusResultRepository = statusResultRepository;
        this.testResultRepository = testResultRepository;
        this.elementResultRepository = elementResultRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.pageRepository = pageRepository;
    }

    /**
     * @param id The id of the @see Audit
     * @param shareCode the share code of the @see Audit
     * @return @see Audit
     */
    @ApiOperation(
            value = "Get an Audit for a given id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping("/{id}/{shareCode}")
    public @ResponseBody
    Audit getAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String shareCode) {
        return auditRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }


    /**
     * Get a json file with the audit information
     * @param id The id of the @see Audit
     * @param shareCode the share code of the @see Audit
     * @return @see Audit
     */
    @ApiOperation(
            value = "Get a json file with the audit information",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping(value="/export/{id}/{sharecode}", produces = "application/json")
    public ResponseEntity<Resource> exportAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String shareCode) {
        Audit audit = auditRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        JSONObject jsonFinalObject = exportAudit(audit);
        byte[] buf = jsonFinalObject.toString().getBytes();              
        HttpHeaders header = setUpHeaders(audit.getName());
        return ResponseEntity
                .ok()
                .headers(header)
                .contentLength(buf.length)
                .contentType(MediaType.parseMediaType("application/json"))
                .body(new ByteArrayResource(buf));
    }

    /**
     * Get all @see Audit for a given project id
     *
     * @param id The id of the @see Project
     * @return A collection of @see Audit
     */
    @ApiOperation(
            value = "Get all audits for a given Project id",
            notes = "User must have SHOW_AUDIT authority on project"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
            "#id)")
    @GetMapping("/by-project/{id}")
    public @ResponseBody
    Collection<Audit> getAuditsByProject(@PathVariable long id) {
        return auditService.findAllByProject(projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project " + id)));
    }

    @ApiOperation(
            value = "Get last Audit by project id",
            notes = "User must have SHOW_AUDIT authority on project",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
            "#id)")
    @GetMapping("/last-by-project/{id}")
    public @ResponseBody
    Audit getLastAuditByProject(@PathVariable long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project for id " + id));
        Optional<Act> actOptional = actRepository.findFirstByProjectOrderByDateDesc(project);
        return actOptional.map(Act::getAudit).orElse(null);
    }

    @ApiOperation(
            value = "Get last Audit by project id and audit type",
            notes = "User must have SHOW_AUDIT authority on project",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
            "#id)")
    @GetMapping("/last-by-project/{id}/{type}")
    public @ResponseBody
    Audit getLastAuditByProjectAndAuditType(@PathVariable long id, @PathVariable EAuditType type) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project for id " + id));
        Optional<Act> actOptional = actRepository.findFirstByProjectAndAudit_TypeOrderByDateDesc(project, type);
        return actOptional.map(Act::getAudit).orElse(null);
    }

    /**
     * @param demoCommandDTO The @see DemoCommandDTO of the demo audit
     * @return @see AuditRequest
     */
    @ApiOperation(
            value = "Start a demo audit",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters")
    })
    @PostMapping("/demo")
    public @ResponseBody
    Audit startDemo(@RequestBody DemoCommandDTO demoCommandDTO) {
        Map<EAuditParameter, String> auditParameters = new HashMap<>();
        auditParameters.put(EAuditParameter.PAGE_URLS, demoCommandDTO.getUrl());
        TestHierarchy testHierarchy = testHierarchyRepository.getOne(1L);
        Audit audit = auditFactory.createAudit(
                demoCommandDTO.getUrl(),
                auditParameters,
                EAuditType.PAGE,
                false,
                null,
                new ArrayList<>(Collections.singletonList(testHierarchy)),
                testHierarchy);

        auditRunnerService.runAudit(audit);
        return audit;
    }

    /**
     * @param auditCommand The @see AuditCommandDTO with bare bones data to start an audit
     * @return @see AuditRequest
     */
    @ApiOperation(
            value = "Start an audit",
            notes = "User must have START_AUDIT authority on project",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).START_AUDIT, " +
            "#auditCommand.getProjectId())")
    @PostMapping("/start")
    public @ResponseBody
    Audit startAudit(@RequestBody @Valid AuditCommandDTO auditCommand) {
        if(!auditCommand.getReferences().contains(auditCommand.getMainReference())){
            throw new InvalidEntityException("Main reference is not in the reference list");
        }

        Project project = projectRepository.findById(auditCommand.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project " + auditCommand.getProjectId()));

        if(new Date().after(project.getContract().getDateEnd())){
            throw new ForbiddenException("Contract end date is passed");
        }

        TestHierarchy main = null;
        ArrayList<TestHierarchy> references = new ArrayList<>();
        for(Long referenceId : auditCommand.getReferences()){
            TestHierarchy testHierarchy = testHierarchyRepository.findByIdAndIsDeletedIsFalseAndParentIsNull(referenceId)
                    .orElseThrow(() -> new InvalidEntityException("Cannot find usable reference for id " + referenceId));
            if(testHierarchy.getId() == auditCommand.getMainReference()){
                main = testHierarchy;
            }
            references.add(testHierarchy);
        }

        Audit audit = auditFactory.createAudit(
                auditCommand.getName(),
                auditCommand.getParameters(),
                auditCommand.getType(),
                true,
                project,
                new ArrayList<>(references),
                main
                );

        auditRunnerService.runAudit(audit);
        return audit;
    }

    /**
     * @param id The id of the @see Audit to delete
     */
    @ApiOperation(
            value = "Delete an audit by id",
            notes = "User must have DELETE_AUDIT authority on project"
            )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanDeleteAudit(#id)")
    @DeleteMapping("/{id}")
    public @ResponseBody
    void deleteAudit(@PathVariable long id) {
        auditService.deleteAudit(auditRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new));
    }

    /**
     * Set the headers settings
     * @return httpheaders with the settings
     */
    private HttpHeaders setUpHeaders(String filename) {
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Disposition", "attachment; filename=\""+filename+"\"");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return header;
    }
    
    /**
     * Return information of the audit as a json object
     * @param audit the audit to export
     * @return json object of the audit
     */
    private JSONObject exportAudit(Audit audit) {
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
     * @throws JSONException
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
     * @throws JSONException
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
    private void addTestHierarchy(JSONObject pageResultObject, Page page, long referenceTestHierarchyId) {
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