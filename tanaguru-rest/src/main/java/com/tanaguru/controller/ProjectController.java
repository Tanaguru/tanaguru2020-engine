package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.constant.EProjectRole;
import com.tanaguru.domain.constant.ProjectAuthorityName;
import com.tanaguru.domain.dto.ProjectDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.helper.UrlHelper;
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
                    + "\nIf contract not found, exception raise : CONTRACT_NOT_FOUND with contract id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found : CONTRACT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).SHOW_CONTRACT, " +
                    "#id)")
    @GetMapping(value = "/member-of/by-contract/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Collection<Project> findAllByContractAndCurrentUserIsMemberOf(@PathVariable long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, id ));
        return projectService.findAllByContractAndUser(
                contract,
                tanaguruUserDetailsService.getCurrentUser()
        );
    }

    @ApiOperation(
            value = "Get All projects current user has authority on for a given Contract id",
            notes = "User must must have SHOW_CONTRACT authority on contract"
                    + "\nIf contract not found, exception raise : CONTRACT_NOT_FOUND with contract id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found : CONTRACT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnContract(" +
                    "T(com.tanaguru.domain.constant.ContractAuthorityName).SHOW_CONTRACT, " +
                    "#id)")
    @GetMapping(value = "/by-contract/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Collection<Project> findAllWithAuthoritiesByContract(@PathVariable long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, id ));

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
            value = "Get Project for a given Audit id",
            notes = "If audit not found, exception raise : AUDIT_NOT_FOUND with audit id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping(value = "/by-audit/{id}/{shareCode}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Project findByAuditId(@PathVariable long id,
                          @ApiParam(required = false) @PathVariable(required = false) String shareCode) {

        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id ));

        return projectService.findByAudit(audit)
                .orElse(null);
    }

    /**
     * @return Get one @see Project
     */
    @ApiOperation(
            value = "Get Project by id",
            notes = "User must have SHOW_PROJECT authority on project"
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
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_PROJECT, " +
                    "#id)")
    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Project findById(@PathVariable long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id ));
    }

    /**
     * @return Get current @see ProjectAuthority names for a given @see Project
     */
    @ApiOperation(
            value = "Get current User authorities on project",
            notes = "If project not found, exception raise : PROJECT_NOT_FOUND with project id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error")
    })
    @GetMapping(value = "/{id}/authorities", produces = {MediaType.APPLICATION_JSON_VALUE})
    public @ResponseBody
    Collection<String> findAuthoritiesByProjectId(@PathVariable long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id ));

        return projectService.getUserAuthoritiesOnProject(tanaguruUserDetailsService.getCurrentUser(), project);
    }

    @ApiOperation(
            value = "Create a Project",
            notes = "User must have CREATE_PROJECT authority on Contract"
                    + "\nIf contract not found, exception raise : CONTRACT_NOT_FOUND with contract id"
                    + "\nIf project limit is greater or equals than the number of project, exception raise : PROJECT_LIMIT_FOR_CONTRACT with contract id and the limit number"
                    + "\nIf the project domain is invalid, exception raise : INVALID_DOMAIN with project domain"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Contract not found : CONTRACT_NOT_FOUND error"
                    + "\nProject limit for contract : PROJECT_LIMIT_FOR_CONTRACT error"
                    + "\nInvalid domain : INVALID_DOMAIN error")
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CONTRACT_NOT_FOUND, project.getContractId() ));

        if(contract.getProjectLimit() > 0 && contract.getProjects().size() >= contract.getProjectLimit()) {
            throw new CustomForbiddenException(CustomError.PROJECT_LIMIT_FOR_CONTRACT, contract.getId(), contract.getProjectLimit()  );
        }

        if((contract.isRestrictDomain() &&
            !project.getDomain().isEmpty() &&
            !UrlHelper.isValid(project.getDomain())) ||
                    (!contract.isRestrictDomain() &&
                    !UrlHelper.isValid(project.getDomain()))){
            throw new CustomInvalidEntityException(CustomError.INVALID_DOMAIN, project.getDomain() );
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
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).DELETE_PROJECT, " +
                    "#id)")
    @DeleteMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void deleteProject(@PathVariable long id) {
        projectService.deleteProject(
                projectRepository.findById(id)
                    .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, id ))
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
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id"
                    + "\nIf user not found, exception raise : USER_NOT_FOUND with user id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error"
                    + "\nUser not found : USER_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).INVITE_MEMBER, " +
                    "#projectId)")
    @PutMapping(value = "/{projectId}/add-member/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProjectAppUser addMember(@PathVariable long projectId, @PathVariable long userId){
        return projectService.addMember(
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, projectId )),
                userRepository.findById(userId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, userId )));
    }

    /**
     * Delete a @see ProjectAppUser
     * @param userId The @see User id to remove from @see Project
     * @param projectId The @see Project id to remove the @see User from
     */
    @ApiOperation(
            value = "Remove a member of a Project",
            notes = "User must have REMOVE_MEMBER authority on Project"
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id"
                    + "\nIf user not found, exception raise : USER_NOT_FOUND with user id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error"
                    + "\nUser not found : USER_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).REMOVE_MEMBER, " +
                    "#projectId)")
    @PutMapping(value = "/{projectId}/remove-member/{userId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public void removeMember(@PathVariable long projectId, @PathVariable long userId){
        projectService.removeMember(
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, projectId )),
                userRepository.findById(userId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, userId ))
        );
    }

    @ApiOperation(
            value = "Promote a member of a Project",
            notes = "User must have PROMOTE_MEMBER authority on Project"
                    + "\nIf user try to promote himself, exception raise : CANNOT_PROMOTE_YOURSELF"
                    + "\nIf project cannot promote user, exception raise :  PROJECT_CANNOT_PROMOTE_USER"
                    + "\nIf project not found, exception raise : PROJECT_NOT_FOUND with project id"
                    + "\nIf user not found, exception raise : USER_NOT_FOUND with user id"
                    + "\nIf user is not found, for the project, exception raise : USER_NOT_FOUND_FOR_PROJECT with user id and project id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or try to self promote"),
            @ApiResponse(code = 404, message = "Cannot promote yourself : CANNOT_PROMOTE_YOURSELF error"
                    + "\nProject cannot promote user ; PROJECT_CANNOT_PROMOTE_USER error"
                    + "\nProject not found : PROJECT_NOT_FOUND error"
                    + "\nUser not found : USER_NOT_FOUND error"
                    + "\nUser not found for the project : USER_NOT_FOUND_FOR_PROJECT error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).PROMOTE_MEMBER, " +
                    "#projectId)")
    @PutMapping(value = "/{projectId}/promote-member/{userId}/to/{projectRole}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ProjectAppUser promoteMember(@PathVariable long projectId, @PathVariable long userId, @PathVariable EProjectRole projectRole){
        User current = tanaguruUserDetailsService.getCurrentUser();
        if(current.getId() == userId){
            throw new CustomForbiddenException(CustomError.CANNOT_PROMOTE_YOURSELF);
        }

        if(projectService.getProjectRole(projectRole).isHidden()){
            throw new CustomInvalidEntityException(CustomError.PROJECT_CANNOT_PROMOTE_USER);
        }

        ProjectAppUser target = projectUserRepository.findByProjectAndContractAppUser_User(
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, projectId )),
                userRepository.findById(userId)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND, userId ))
        ).orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND_FOR_PROJECT, userId, projectId ));

        target.setProjectRole(projectService.getProjectRole(projectRole));
        return projectUserRepository.save(target);
    }

}
