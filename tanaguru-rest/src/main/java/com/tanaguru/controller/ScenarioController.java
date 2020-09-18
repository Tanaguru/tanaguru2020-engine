package com.tanaguru.controller;

import com.tanaguru.domain.constant.ProjectAuthorityName;
import com.tanaguru.domain.dto.ScenarioDTO;
import com.tanaguru.domain.entity.audit.Scenario;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.exception.ForbiddenException;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.ScenarioRepository;
import com.tanaguru.service.ProjectService;
import com.tanaguru.service.ScenarioService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Base64;
import java.util.Collection;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/scenarios")
public class ScenarioController {
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final ScenarioRepository scenarioRepository;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final ScenarioService scenarioService;

    public ScenarioController(TanaguruUserDetailsService tanaguruUserDetailsService, ScenarioRepository scenarioRepository, ProjectService projectService, ProjectRepository projectRepository, ScenarioService scenarioService) {
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.scenarioRepository = scenarioRepository;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.scenarioService = scenarioService;
    }

    /**
     * @param id The id of the @see Scenario
     * @return @see Scenario
     */
    @ApiOperation(
            value = "Get Scenario for a given id",
            notes = "User must have SHOW_PROJECT authority on Project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Scenario not found")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping("/{id}")
    public @ResponseBody
    Scenario getScenario(@PathVariable long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        if(projectService.hasAuthority(
                tanaguruUserDetailsService.getCurrentUser(),
                ProjectAuthorityName.SHOW_PROJECT,
                scenario.getProject(),
                true)){
            return scenario;
        }else{
            throw new ForbiddenException("Current user has no access to scenario " + id);
        }
    }

    /**
     * @param id The @see Project id
     * @return All the associated @see Scenario
     */
    @ApiOperation(
            value = "Get all Scenario for a given project id",
            notes = "User must have SHOW_PROJECT authority on Project"
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
    @GetMapping("/by-project/{id}")
    public @ResponseBody
    Collection<Scenario> getAllByProject(@PathVariable long id) {
        return scenarioRepository.findAllByProject_IdAndIsDeletedIsFalse(id);
    }

    /**
     * @param scenarioDTO The data to create the @see Scenario
     * @return @see Scenario
     */
    @ApiOperation(
            value = "Create Scenario for a given project id",
            notes = "User must have ADD_SCENARIO authority on Project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).ADD_SCENARIO, " +
                    "#scenarioDTO.getProjectId())")
    @PostMapping("/")
    public @ResponseBody
    Scenario createScenario(@RequestBody @Valid ScenarioDTO scenarioDTO) {
        Project project = projectRepository.findById(scenarioDTO.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find project " + scenarioDTO.getProjectId()));

        String content = new String(Base64.getDecoder().decode(scenarioDTO.getContent()));

        if(! scenarioService.checkScenarioIsValid(content)){
            throw new InvalidEntityException("Scenario content is invalid");
        }

        Scenario scenario = new Scenario();
        scenario.setContent(scenarioDTO.getContent());
        scenario.setName(scenarioDTO.getName());
        scenario.setProject(project);
        return scenarioRepository.save(scenario);
    }

    /**
     * @param id The id of the @see Scenario to delete
     */
    @ApiOperation(
            value = "Delete Scenario for a given id",
            notes = "User must have DELETE_SCENARIO authority on Project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Resource not found")
    })
    @DeleteMapping("/{id}")
    public @ResponseBody
    void deleteAudit(@PathVariable long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        if(projectService.hasAuthority(
                tanaguruUserDetailsService.getCurrentUser(),
                ProjectAuthorityName.DELETE_SCENARIO,
                scenario.getProject(),
                true)) {
            scenario.setDeleted(true);
            scenarioRepository.save(scenario);
        }else{
            throw new ForbiddenException("User has no authority to delete the scenario " + id);
        }
    }
}
