package com.tanaguru.controller;

import com.tanaguru.constant.CustomError;
import com.tanaguru.custom.exception.CustomEntityNotFoundException;
import com.tanaguru.custom.exception.CustomForbiddenException;
import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.dto.AuditCommandDTO;
import com.tanaguru.domain.dto.DemoCommandDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.factory.AuditFactory;
import com.tanaguru.repository.*;
import com.tanaguru.service.*;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.lang.reflect.Array;
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

    @Autowired
    public AuditController(
            AuditRepository auditRepository,
            AuditService auditService, AuditFactory auditFactory,
            AuditRunnerService auditRunnerService,
            ProjectRepository projectRepository,
            ActRepository actRepository,
            TestHierarchyRepository testHierarchyRepository) {

        this.auditRepository = auditRepository;
        this.auditService = auditService;
        this.auditFactory = auditFactory;
        this.auditRunnerService = auditRunnerService;
        this.projectRepository = projectRepository;
        this.actRepository = actRepository;
        this.testHierarchyRepository = testHierarchyRepository;
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id)));
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id));
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
            throw new CustomInvalidEntityException(CustomError.NO_MAIN_REFERENCE);
        }

        Project project = projectRepository.findById(auditCommand.getProjectId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, auditCommand.getProjectId()));

        if(new Date().after(project.getContract().getDateEnd())){
            throw new CustomForbiddenException(CustomError.CONTRACT_DATE_PASSED);
        }

        TestHierarchy main = null;
        ArrayList<TestHierarchy> references = new ArrayList<>();
        for(Long referenceId : auditCommand.getReferences()){
            TestHierarchy testHierarchy = testHierarchyRepository.findByIdAndIsDeletedIsFalseAndParentIsNull(referenceId)
                    .orElseThrow(() -> new CustomInvalidEntityException(CustomError.NO_USABLE_REFERENCE, referenceId));
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
                .orElseThrow(CustomEntityNotFoundException::new));
    }
}
