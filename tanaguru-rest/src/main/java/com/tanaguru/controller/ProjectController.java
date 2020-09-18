package com.tanaguru.controller;

import com.tanaguru.domain.constant.EProjectRole;
import com.tanaguru.domain.constant.ProjectAuthorityName;
import com.tanaguru.domain.dto.ProjectDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.ForbiddenException;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.*;
import com.tanaguru.service.ProjectService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;
    private final ContractUserRepository contractUserRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final AuditRepository auditRepository;

    @Autowired
    public ProjectController(
            ProjectService projectService,
            TanaguruUserDetailsService tanaguruUserDetailsService,
            ProjectRepository projectRepository,
            ContractRepository contractRepository,
            ContractUserRepository contractUserRepository,
            ProjectUserRepository projectUserRepository, UserRepository userRepository, AuditRepository auditRepository) {

        this.projectService = projectService;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.projectRepository = projectRepository;
        this.contractRepository = contractRepository;
        this.contractUserRepository = contractUserRepository;
        this.projectUserRepository = projectUserRepository;
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
    }
    @ApiOperation(
            value = "Get All projects current user is member of for a given Contract id",
            notes = "User must must have SHOW_CONTRACT authority on contract"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).SHOW_CONTRACT, " +
                    "#id)")
    @GetMapping(value = "/member-of/by-contract/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Collection<Project> findAllByContractAndCurrentUserIsMemberOf(@PathVariable long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find contract " + id));
        return projectService.findAllByContractAndUser(
                contract,
                tanaguruUserDetailsService.getCurrentUser()
        );
    }

    @ApiOperation(
            value = "Get All projects current user has authority on for a given Contract id",
            notes = "User must must have SHOW_CONTRACT authority on contract"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).SHOW_CONTRACT, " +
                    "#id)")
    @GetMapping(value = "/by-contract/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Collection<Project> findAllWithAuthoritiesByContract(@PathVariable long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find contract " + id));

        ContractAppUser contractAppUser = contractUserRepository.findByContractAndContractRoleName_Owner(contract);

        return projectService.hasOverrideAuthority(tanaguruUserDetailsService.getCurrentUser(), ProjectAuthorityName.SHOW_PROJECT) ||
                contractAppUser.getUser().getId() == tanaguruUserDetailsService.getCurrentUser().getId() ?
                projectRepository.findAllByContract(contract) :
                findAllByContractAndCurrentUserIsMemberOf(id);
    }

    /**
     * @return Get one @see Project
     */
    @ApiOperation(
            value = "Get Project for a given Audit id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping(value = "/by-audit/{id}/{shareCode}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Project findByAuditId(@PathVariable long id,
                          @ApiParam(required = false) @PathVariable(required = false) String shareCode) {

        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit " + id));

        return projectService.findByAudit(audit)
                .orElse(null);
    }

    /**
     * @return Get one @see Project
     */
    @ApiOperation(
            value = "Get Project by id",
            notes = "User must have SHOW_PROJECT authority on project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_PROJECT, " +
                    "#id)")
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Project findById(@PathVariable long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project " + id));
    }

    /**
     * @return Get current @see ProjectAuthority names for a given @see Project
     */
    @ApiOperation(
            value = "Get current User authorities on project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @GetMapping(value = "/{id}/authorities", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<String> findAuthoritiesByProjectId(@PathVariable long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project " + id));

        return projectService.getUserAuthoritiesOnProject(tanaguruUserDetailsService.getCurrentUser(), project);
    }

    @ApiOperation(
            value = "Create a Project",
            notes = "User must have CREATE_PROJECT authority on Contract"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).CREATE_PROJECT, " +
                    "#project.getContractId())")
    @PostMapping(value = "/", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Project createProject(@RequestBody @Valid ProjectDTO project) {
        User user = tanaguruUserDetailsService.getCurrentUser();

        Contract contract = contractRepository.findById(project.getContractId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find contract " + project.getContractId()));

        if(contract.getProjectLimit() > 0 && contract.getProjects().size() >= contract.getProjectLimit()) {
            throw new ForbiddenException("Project limit for contract " + contract.getId() + " is " + contract.getProjectLimit());
        }

        UrlValidator urlValidator = new UrlValidator();
        if(!urlValidator.isValid(project.getDomain())){
            throw new InvalidEntityException("Domain " + project.getDomain() + " is invalid");
        }

        // If the current user is an admin that is not member of the contract, set the contract owner as default member of the project
        ContractAppUser contractAppUser = contractUserRepository.findByContractAndUser(contract, user)
                .orElseGet(() -> contractUserRepository.findByContractAndContractRoleName_Owner(contract));

        Project newProject = projectService.createProject(contract, project.getName(), project.getDomain());
        ProjectAppUser projectAppUser = new ProjectAppUser();
        projectAppUser.setProjectRole(projectService.getProjectRole(EProjectRole.PROJECT_MANAGER));
        projectAppUser.setContractAppUser(contractAppUser);
        projectAppUser.setProject(newProject);
        projectUserRepository.save(projectAppUser);

        return newProject;
    }

    @ApiOperation(
            value = "Delete a Project",
            notes = "User must have DELETE_PROJECT authority on Contract"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).DELETE_PROJECT, " +
                    "#id)")
    @DeleteMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteProject(@PathVariable long id) {
        projectService.deleteProject(
                projectRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find project with id " + id))
        );
    }

    /**
     * Add an @see User to a @see Project
     * @param userId The @see User id to add
     * @param projectId The @see targeted project id
     */
    @ApiOperation(
            value = "Add a member to a Project",
            notes = "User must have INVITE_MEMBER authority on Project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project or User not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).INVITE_MEMBER, " +
                    "#projectId)")
    @PutMapping(value = "/{projectId}/add-member/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProjectAppUser addMember(@PathVariable long projectId, @PathVariable long userId){
        return projectService.addMember(
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find project with id " + projectId)),
                userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find user with id " + userId)));
    }

    /**
     * Delete a @see ProjectAppUser
     * @param userId The @see User id to remove from @see Project
     * @param projectId The @see Project id to remove the @see User from
     */
    @ApiOperation(
            value = "Remove a member of a Project",
            notes = "User must have REMOVE_MEMBER authority on Project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project or User not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).REMOVE_MEMBER, " +
                    "#projectId)")
    @PutMapping(value = "/{projectId}/remove-member/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removeMember(@PathVariable long projectId, @PathVariable long userId){
        projectService.removeMember(
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find project with id " + projectId)),
                userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find user with id " + userId))
        );
    }

    @ApiOperation(
            value = "Promote a member of a Project",
            notes = "User must have PROMOTE_MEMBER authority on Project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or try to self promote"),
            @ApiResponse(code = 404, message = "Project, ProjectRole or User not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).PROMOTE_MEMBER, " +
                    "#projectId)")
    @PutMapping(value = "/{projectId}/promote-member/{userId}/to/{projectRole}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProjectAppUser promoteMember(@PathVariable long projectId, @PathVariable long userId, @PathVariable EProjectRole projectRole){
        User current = tanaguruUserDetailsService.getCurrentUser();
        if(current.getId() == userId){
            throw new ForbiddenException("Cannot promote yourself");
        }

        if(projectService.getProjectRole(projectRole).isHidden()){
            throw new InvalidEntityException("This project role cannot be used to promote a user");
        }

        ProjectAppUser target = projectUserRepository.findByProjectAndContractAppUser_User(
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find project with id " + projectId)),
                userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find user with id " + userId))
        ).orElseThrow(() -> new EntityNotFoundException("Cannot find user with id " + userId + " in project " + projectId));

        target.setProjectRole(projectService.getProjectRole(projectRole));
        return projectUserRepository.save(target);
    }

}
