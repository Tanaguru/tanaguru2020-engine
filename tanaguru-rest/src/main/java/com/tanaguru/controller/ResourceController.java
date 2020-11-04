package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.domain.constant.ProjectAuthorityName;
import com.tanaguru.domain.dto.ResourceDTO;
import com.tanaguru.domain.entity.audit.Resource;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.ResourceRepository;
import com.tanaguru.service.ProjectService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Base64;
import java.util.Collection;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/resources")
public class ResourceController {
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final ResourceRepository resourceRepository;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;

    public ResourceController(TanaguruUserDetailsService tanaguruUserDetailsService, ResourceRepository resourceRepository, ProjectService projectService, ProjectRepository projectRepository) {
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.resourceRepository = resourceRepository;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
    }

    /**
     * @param id The id of the @see Resource
     * @return @see Resource
     */
    @ApiOperation(
            value = "Get Resource for a given id",
            notes = "User must have SHOW_PROJECT authority on Project"
                    + "\nIf resource not found, exception raise : RESOURCE_NOT_FOUND with resource id"
                    + "\nIf user cannot access the resource, exception raise : USER_CANNOT_ACCESS_RESOURCE with resource id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Resource not found : RESOURCE_NOT_FOUND error"
                    + "\nUser cannot access resource : USER_CANNOT_ACCESS_RESOURCE error")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping("/{id}")
    public @ResponseBody
    Resource getResource(@PathVariable long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new CustomInvalidEntityException(CustomError.RESOURCE_NOT_FOUND, id ));

        if(projectService.hasAuthority(
                tanaguruUserDetailsService.getCurrentUser(),
                ProjectAuthorityName.SHOW_PROJECT,
                resource.getProject(),
                true)){
            return resource;
        }else{
            throw new CustomForbiddenException(CustomError.USER_CANNOT_ACCESS_RESOURCE, id );
        }
    }

    /**
     * @param id The @see Project id
     * @return All the associated @see Resource
     */
    @ApiOperation(
            value = "Get all Resource for a given project id",
            notes = "User must have SHOW_PROJECT authority on Project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_PROJECT, " +
                    "#id)")
    @GetMapping("/by-project/{id}")
    public @ResponseBody
    Collection<Resource> getAllByProject(@PathVariable long id) {
            return resourceRepository.findAllByProject_IdAndIsDeletedIsFalse(id);
    }

    /**
     * @param resourceDTO The data to create the @see Resource
     * @return @see Resource
     */
    @ApiOperation(
            value = "Create Resource for a given project id",
            notes = "User must have ADD_RESOURCE authority on Project"
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
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).ADD_RESOURCE, " +
                    "#resourceDTO.getProjectId())")
    @PostMapping("/")
    public @ResponseBody
    Resource createResource(@RequestBody @Valid ResourceDTO resourceDTO) {
        Project project = projectRepository.findById(resourceDTO.getProjectId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, resourceDTO.getProjectId() ));

        Resource resource = new Resource();
        resource.setContent(
                new String(Base64.getDecoder().decode(resourceDTO.getContent()))
        );
        resource.setName(resourceDTO.getName());
        resource.setProject(project);
        return resourceRepository.save(resource);
    }

    /**
     * @param id The id of the @see Resource to delete
     */
    @ApiOperation(
            value = "Delete Resource for a given id",
            notes = "User must have DELETE_RESOURCE authority on Project"
                    + "\nIf resource not found, exception raise : RESOURCE_NOT_FOUND, with resource id"
                    + "\nIf user cannot delete the resource, exception raise : USER_CANNOT_DELETE_RESOURCE with resource id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Resource not found : RESOURCE_NOT_FOUND error"
                    + "\nUser cannot delete resource : USER_CANNOT_DELETE_RESOURCE error")
    })
    @DeleteMapping("/{id}")
    public @ResponseBody
    void deleteResource(@PathVariable long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.RESOURCE_NOT_FOUND, id ));

        if(projectService.hasAuthority(
                tanaguruUserDetailsService.getCurrentUser(),
                ProjectAuthorityName.DELETE_RESOURCE,
                resource.getProject(),
                true)) {

            resource.setDeleted(true);
            resourceRepository.save(resource);
        }else{
            throw new CustomForbiddenException(CustomError.USER_CANNOT_DELETE_RESOURCE, id );
        }
    }
}
