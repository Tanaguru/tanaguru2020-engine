package com.tanaguru.controller;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAuditStatus;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
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
import com.tanaguru.helper.JsonHttpHeaderBuilder;
import com.tanaguru.repository.*;
import com.tanaguru.service.*;

import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.*;

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
    private final static Long SHM_SIZE = 2147483648L; //2gb
    
    @Value("${auditrunner.audit-docker.enabled}")
    private boolean auditWithDocker;
    
    @Value("${auditrunner.audit-docker.image}")
    private String imageName;
    
    @Value("${auditrunner.audit-docker.network.mode}")
    private String networkMode;
       
    @Autowired
    public AuditController(
            AuditRepository auditRepository,
            AuditService auditService, AuditFactory auditFactory,
            AuditRunnerService auditRunnerService,
            ProjectRepository projectRepository,
            ActRepository actRepository,
            TestHierarchyRepository testHierarchyRepository, AsyncAuditService asyncAuditService) {

        this.auditRepository = auditRepository;
        this.auditService = auditService;
        this.auditFactory = auditFactory;
        this.auditRunnerService = auditRunnerService;
        this.projectRepository = projectRepository;
        this.actRepository = actRepository;
        this.testHierarchyRepository = testHierarchyRepository;
        this.asyncAuditService = asyncAuditService;
    }

    /**
     * @param id The id of the @see Audit
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id ));
    }

    /**
     * @param id The id of the @see Audit
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id ));

        for(Page page : audit.getPages()){
            if(page.getPageContent().getScreenshot() != null){
                return true;
            }
        }
        return false;

    }


    /**
     * Get a json file with the audit information
     * @param id The id of the @see Audit
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
    @GetMapping(value="/export/{id}/{sharecode}", produces = "application/json")
    public ResponseEntity<Resource> exportAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String shareCode) {
        Audit audit = auditRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        JSONObject jsonFinalObject = auditService.toJson(audit);
        byte[] buf = jsonFinalObject.toString().getBytes();              
        HttpHeaders header = JsonHttpHeaderBuilder.setUpJsonHeaders(audit.getName(),"json");
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id )));
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id ));
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id ));
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
        if(!auditCommand.getReferences().contains(auditCommand.getMainReference())){
            throw new CustomInvalidEntityException(CustomError.NO_MAIN_REFERENCE);
        }

        Project project = projectRepository.findById(auditCommand.getProjectId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, auditCommand.getProjectId() ));

        if(new Date().after(project.getContract().getDateEnd())){
            throw new CustomForbiddenException(CustomError.CONTRACT_DATE_PASSED);
        }

        TestHierarchy main = null;
        ArrayList<TestHierarchy> references = new ArrayList<>();
        for(Long referenceId : auditCommand.getReferences()){
            TestHierarchy testHierarchy = testHierarchyRepository.findByIdAndIsDeletedIsFalseAndParentIsNull(referenceId)
                    .orElseThrow(() -> new CustomInvalidEntityException(CustomError.NO_USABLE_REFERENCE, referenceId ));
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
        
        if(auditWithDocker) {
            runAuditByCli(audit);
        }else{
            auditRunnerService.runAudit(audit);
        }
        return audit;
    }

    private void runAuditByCli(Audit audit) {
        DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();
        
        HostConfig hostConfig = HostConfig
                .newHostConfig()
                .withShmSize(SHM_SIZE)
                .withNetworkMode(networkMode)
                .withAutoRemove(true);
        
        List<Container> containers = dockerClient.listContainersCmd().exec();
        for(Container c : containers) {
            dockerClient.stopContainerCmd(c.getId()).exec();
            dockerClient.removeContainerCmd(c.getId()).exec();
        }

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName+":latest")
                .withHostConfig(hostConfig)
                .withEnv("LANG","fr_FR.UTF-8","LANGUAGE","fr_FR:fr","LC_ALL","fr_FR.UTF-8")
                .withCmd("-auditId",String.valueOf(audit.getId())).exec();
       
        dockerClient.startContainerCmd(container.getId()).exec();
        dockerClient.logContainerCmd(container.getId())
        .withStdErr(true)
        .withStdOut(true)
        .withFollowStream(true)
        .exec(new ResultCallbackTemplate<LogContainerResultCallback, Frame>() {
            @Override
            public void onNext(Frame frame) {
                System.out.print(new String(frame.getPayload()));
            }
        });
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
                .orElseThrow(() -> new CustomInvalidEntityException(CustomError.AUDIT_NOT_FOUND, id ));
        if(audit.getStatus() == EAuditStatus.DONE || audit.getStatus() == EAuditStatus.ERROR){
            asyncAuditService.deleteAudit(auditRepository.findById(id)
                .orElseThrow(() -> new CustomInvalidEntityException(CustomError.AUDIT_NOT_FOUND, id )));
        }else{
            throw new CustomInvalidEntityException(CustomError.CANNOT_DELETE_RUNNING_AUDIT , audit.getId());
        }
        
    }
}