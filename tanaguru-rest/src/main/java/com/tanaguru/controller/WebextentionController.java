package com.tanaguru.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.dto.WebextentionDTO;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.audit.Webextention;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.repository.WebextentionRepository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author lpedrau
 */
@RestController
@RequestMapping("/webextention")
public class WebextentionController {
    private final Logger LOGGER = LoggerFactory.getLogger(WebextentionController.class);

    private final WebextentionRepository webextentionRepository;
    private final TestHierarchyRepository testHierarchyRepository;
    
    @Autowired
    public WebextentionController(WebextentionRepository webextentionRepository, TestHierarchyRepository testHierarchyRepository) {
        this.webextentionRepository = webextentionRepository;
        this.testHierarchyRepository = testHierarchyRepository;
    }
    
    /**
     * @return the webextention version used by engine
     */
    @ApiOperation(
            value = "Get the webextention version used by engine")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session")
    })
    @GetMapping(value = "/version")
    public @ResponseBody
    String getWebextentionVersion() {
        Optional<Webextention> webextention = this.webextentionRepository.findFirstByOrderByIdDesc();
        return webextention.isEmpty() ? "" : webextention.get().getVersion();
    }
    
    /**
     * Upload a webextention version associated with test hierarchy
     *
     * @param webextentionVersion The @see WebextentionDTO with data
     */
    @ApiOperation(
            value = "Upload a webextention version associated with test hierarchy",
            notes = "If test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Test hierarchy not found : TEST_HIERARCHY_NOT_FOUND error")
    })
    @PostMapping(value = "/version", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String createWebextentionVersion(@RequestBody @Valid WebextentionDTO webextentionDto) {
        Webextention webextention = new Webextention();
        TestHierarchy testHierarchy = this.testHierarchyRepository.findById(webextentionDto.getTestHierarchyId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, webextentionDto.getTestHierarchyId()));
        
        webextention.setTestHierarchy(testHierarchy);
        webextention.setVersion(webextentionDto.getVersion());
        this.webextentionRepository.save(webextention);
        return webextention.getVersion();
    }
    
}
