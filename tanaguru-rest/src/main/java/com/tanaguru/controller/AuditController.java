package com.tanaguru.controller;

import com.tanaguru.domain.constant.*;
import com.tanaguru.domain.dto.AuditCommandDTO;
import com.tanaguru.domain.dto.DemoCommandDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.exception.CustomIllegalStateException;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.helper.JsonHttpHeaderBuilder;
import com.tanaguru.repository.ActRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.service.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.*;

import static com.tanaguru.domain.constant.CustomError.FORBIDDEN_STOP_AUDIT;

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
    private final AsyncAuditService asyncAuditService;
    private final ProjectService projectService;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;

    @Autowired
    public AuditController(
            AuditRepository auditRepository,
            AuditService auditService, AuditFactory auditFactory,
            AuditRunnerService auditRunnerService,
            ProjectRepository projectRepository,
            ActRepository actRepository,
            TestHierarchyRepository testHierarchyRepository, AsyncAuditService asyncAuditService, ProjectService projectService, TanaguruUserDetailsService tanaguruUserDetailsService) {

        this.auditRepository = auditRepository;
        this.auditService = auditService;
        this.auditFactory = auditFactory;
        this.auditRunnerService = auditRunnerService;
        this.projectRepository = projectRepository;
        this.actRepository = actRepository;
        this.testHierarchyRepository = testHierarchyRepository;
        this.asyncAuditService = asyncAuditService;
        this.projectService = projectService;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
    }

    /**
     * @param id        The id of the @see Audit
     * @param shareCode the share code of the @see Audit
     * @return @see Audit
     */
    @ApiOperation(
            value = "Get an Audit for a given id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf audit not found, exception raise : AUDIT_NOT_FOUND with audit id",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping("/{id}/{shareCode}")
    public @ResponseBody
    Audit getAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String shareCode) {
        return auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));
    }

    /**
     * @param id        The id of the @see Audit
     * @param shareCode the share code of the @see Audit
     * @return @see number of screenshot in audit
     */
    @ApiOperation(
            value = "Get the number of screenshot in Audit for a given id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf audit not found, exception raise : AUDIT_NOT_FOUND with audit id",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping("/{id}/has-screenshot/{shareCode}")
    public @ResponseBody
    boolean hasScreenshotByAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String shareCode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));

        for (Page page : audit.getPages()) {
            if (page.getPageContent().getScreenshot() != null) {
                return true;
            }
        }
        return false;

    }


    /**
     * Get a json file with the audit information
     *
     * @param id        The id of the @see Audit
     * @param shareCode the share code of the @see Audit
     * @return resource json
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
    @GetMapping(value = "/export/{id}/{sharecode}", produces = "application/json")
    public ResponseEntity<Resource> exportAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String shareCode) {
        Audit audit = auditRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        JSONObject jsonFinalObject = auditService.toJson(audit);
        byte[] buf = jsonFinalObject.toString().getBytes();
        HttpHeaders header = JsonHttpHeaderBuilder.setUpJsonHeaders(audit.getName(), "json");
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
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    "#id)")
    @GetMapping("/by-project/{id}")
    public @ResponseBody
    Collection<Audit> getAuditsByProject(@PathVariable long id) {
        return auditService.findAllByProject(projectRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id)));
    }

    /**
     * Get all @see Audit paginated for a given project id and of a given type
     *
     * @param id   The id of the @see Project
     * @param type The type of the @see Audit
     * @return A collection of @see Audit
     */
    @ApiOperation(
            value = "Get all audits paginated for a given Project id and a given type",
            notes = "User must have SHOW_AUDIT authority on project"
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    "#id)")
    @GetMapping("/by-project-and-type-paginated/{id}/{type}")
    public @ResponseBody
    org.springframework.data.domain.Page<Audit> getAuditsByProjectAndType(@PathVariable long id,
                                                                          @PathVariable EAuditType type,
                                                                          @RequestParam(defaultValue = "0") @ApiParam(required = false) int page,
                                                                          @RequestParam(defaultValue = "5") @ApiParam(required = false) int size,
                                                                          @RequestParam(defaultValue = "id") @ApiParam(required = false) String sortBy,
                                                                          @RequestParam(defaultValue = "false") @ApiParam(required = false) boolean isAsc) {
        Direction direction = (isAsc) ? Direction.ASC : Direction.DESC;
        return auditService.findAllByProjectAndType(projectRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id)), type, PageRequest.of(page, size, Sort.by(direction, sortBy)));
    }

    @ApiOperation(
            value = "Get last Audit by project id",
            notes = "User must have SHOW_AUDIT authority on project"
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    "#id)")
    @GetMapping("/last-by-project/{id}")
    public @ResponseBody
    Audit getLastAuditByProject(@PathVariable long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id));
        Optional<Act> actOptional = actRepository.findFirstByProjectOrderByDateDesc(project);
        return actOptional.map(Act::getAudit).orElse(null);
    }

    @ApiOperation(
            value = "Get last Audit by project id and audit type",
            notes = "User must have SHOW_AUDIT authority on project"
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    "#id)")
    @GetMapping("/last-by-project/{id}/{type}")
    public @ResponseBody
    Audit getLastAuditByProjectAndAuditType(@PathVariable long id, @PathVariable EAuditType type) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id));
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
            notes = "User must have START_AUDIT authority on project"
                    + "\nIf audit command doesn't contains main reference, exception raise : NO_MAIN_REFERENCE"
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id"
                    + "\nIf contract date passed, exception raise : CONTRACT_DATE_PASSED"
                    + "\nOr if reference test hierarchy not found, exception raise : NO_USABLE_REFERENCE with reference id",
            response = Audit.class)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error"
                    + "\nNo main reference : NO_MAIN_REFERENCE error"
                    + "\nContract date passed : CONTRACT_DATE_PASSED error"
                    + "\nTest hierarchy reference not found : NO_USABLE_REFERENCE error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).START_AUDIT, " +
                    "#auditCommand.getProjectId())")
    @PostMapping("/start")
    public @ResponseBody
    Audit startAudit(@RequestBody @Valid AuditCommandDTO auditCommand) {
        if (!auditCommand.getReferences().contains(auditCommand.getMainReference())) {
            throw new CustomInvalidEntityException(CustomError.NO_MAIN_REFERENCE);
        }

        Project project = projectRepository.findById(auditCommand.getProjectId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, auditCommand.getProjectId()));

        if (new Date().after(project.getContract().getDateEnd())) {
            throw new CustomForbiddenException(CustomError.CONTRACT_DATE_PASSED);
        }

        TestHierarchy main = null;
        ArrayList<TestHierarchy> references = new ArrayList<>();
        for (Long referenceId : auditCommand.getReferences()) {
            TestHierarchy testHierarchy = testHierarchyRepository.findByIdAndIsDeletedIsFalseAndParentIsNull(referenceId)
                    .orElseThrow(() -> new CustomInvalidEntityException(CustomError.NO_USABLE_REFERENCE, referenceId));
            if (testHierarchy.getId() == auditCommand.getMainReference()) {
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
     * @param id TheAudit id
     */
    @ApiOperation(
            value = "Stop an audit",
            notes = "User must have START_AUDIT authority on project")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters, CANNOT_STOP_FINISHED_AUDIT error"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "FORBIDDEN_STOP_AUDIT when forbidden for the current session"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
    })

    @PostMapping("/{id}/stop")
    public @ResponseBody
    void stopAudit(@PathVariable long id) {

        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));

        if (audit.isPrivate() && !projectService.findByAudit(audit).map(
                project -> tanaguruUserDetailsService.currentUserHasAuthorityOnProject(
                        ProjectAuthorityName.START_AUDIT,
                        project.getId())).orElse(false)){
            throw new CustomForbiddenException(FORBIDDEN_STOP_AUDIT);
        }

        if (audit.getStatus() == EAuditStatus.DONE && audit.getStatus() == EAuditStatus.ERROR) {
            throw new CustomInvalidEntityException(CustomError.CANNOT_STOP_FINISHED_AUDIT, id);
        }
        try {
            auditRunnerService.stopAudit(audit);
        } catch (Exception e) {
            throw new CustomIllegalStateException(CustomError.CANNOT_STOP_AUDIT_WITH_CURRENT_CONFIGURATION);
        }
    }

    /**
     * @param id The id of the @see Audit to delete
     */
    @ApiOperation(
            value = "Delete an audit by id",
            notes = "User must have DELETE_AUDIT authority on project"
                    + "\nIf audit not found, exception raise: AUDIT_NOT_FOUND with audit id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanDeleteAudit(#id)")
    @DeleteMapping("/{id}")
    public @ResponseBody
    void deleteAudit(@PathVariable long id) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomInvalidEntityException(CustomError.AUDIT_NOT_FOUND, id));

        if (audit.getStatus() == EAuditStatus.DONE || audit.getStatus() == EAuditStatus.ERROR) {
            audit.setDeleted(true);
            audit = auditRepository.save(audit);
            asyncAuditService.deleteAudit(audit);
        } else {
            throw new CustomInvalidEntityException(CustomError.CANNOT_DELETE_RUNNING_AUDIT, audit.getId());
        }

    }
}