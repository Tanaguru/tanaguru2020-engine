package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.ProjectAuthorityName;
import com.tanaguru.domain.dto.ScenarioDTO;
import com.tanaguru.domain.entity.audit.Scenario;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
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
                    + "\nIf scenario not found, exception raise : SCENARIO_NOT_FOUND with scenario id"
                    + "\nIf user cannot access the scenario, exception raise : USER_CANNOT_ACCESS_SCENARIO with scenario id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Scenario not found : SCENARIO_NOT_FOUND error"
                    + "\nUser cannot access scenario : USER_CANNOT_ACCESS_SCENARIO error")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping("/{id}")
    public @ResponseBody
    Scenario getScenario(@PathVariable long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.SCENARIO_NOT_FOUND, id ));

        if(projectService.hasAuthority(
                tanaguruUserDetailsService.getCurrentUser(),
                ProjectAuthorityName.SHOW_PROJECT,
                scenario.getProject(),
                true)){
            return scenario;
        }else{
            throw new CustomForbiddenException(CustomError.USER_CANNOT_ACCESS_SCENARIO, id );
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
                    + "\nIf project not found exception raise : PROJECT_NOT_FOUND with project id"
                    + "\nIf invalid scenario content, exception raise : INVALID_SCENARIO_CONTENT"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Project not found : PROJECT_NOT_FOUND error"
                    + "\nInvalid scenario content : INVALID_SCENARIO_CONTENT error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    "T(com.tanaguru.domain.constant.ProjectAuthorityName).ADD_SCENARIO, " +
                    "#scenarioDTO.getProjectId())")
    @PostMapping("/")
    public @ResponseBody
    Scenario createScenario(@RequestBody @Valid ScenarioDTO scenarioDTO) {
        Project project = projectRepository.findById(scenarioDTO.getProjectId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_NOT_FOUND, scenarioDTO.getProjectId() ));

        String content = new String(Base64.getDecoder().decode(scenarioDTO.getContent()));

        if(! scenarioService.checkScenarioIsValid(content)){
            throw new CustomInvalidEntityException(CustomError.INVALID_SCENARIO_CONTENT);
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
                    + "\nIf scenario not found, exception raise : SCENARIO_NOT_FOUND with scenario id"
                    + "\nIf user cannot delete the scenario, exception raise : USER_CANNOT_DELETE_SCENARIO with scenario id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Scenario not found : SCENARIO_NOT_FOUND error"
                    + "\nUser cannot delete scenario : USER_CANNOT_DELETE_SCENARIO error")
    })
    @DeleteMapping("/{id}")
    public @ResponseBody
    void deleteAudit(@PathVariable long id) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.SCENARIO_NOT_FOUND, id ));

        if(projectService.hasAuthority(
                tanaguruUserDetailsService.getCurrentUser(),
                ProjectAuthorityName.DELETE_SCENARIO,
                scenario.getProject(),
                true)) {
            scenario.setDeleted(true);
            scenarioRepository.save(scenario);
        }else{
            throw new CustomForbiddenException(CustomError.USER_CANNOT_DELETE_SCENARIO, id );
        }
    }
}
