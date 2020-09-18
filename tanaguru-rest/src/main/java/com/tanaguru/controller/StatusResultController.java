package com.tanaguru.controller;

import com.tanaguru.domain.dto.StatusResultDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.exception.ForbiddenException;
import com.tanaguru.repository.*;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/status-results")
public class StatusResultController {
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final PageRepository pageRepository;
    private final TestHierarchyRepository testHierarchyRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    private final StatusResultRepository statusResultRepository;
    private final AuditRepository auditRepository;

    public StatusResultController(TanaguruUserDetailsService tanaguruUserDetailsService, PageRepository pageRepository, TestHierarchyRepository testHierarchyRepository, AuditReferenceRepository auditReferenceRepository, StatusResultRepository statusResultRepository, AuditRepository auditRepository) {
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.pageRepository = pageRepository;
        this.testHierarchyRepository = testHierarchyRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.statusResultRepository = statusResultRepository;
        this.auditRepository = auditRepository;
    }

    /**
     * Return a @see StatusResult for a given @see Page and @see TestHierarchy
     * @param pageId The id of the @see Page
     * @param testHierarchyId The id of the @see TestHierarchy
     * @param sharecode The @see Audit sharecode
     * @return A @see StatusResult
     */

    @ApiOperation(
            value = "Get a StatusResult for a given Page id and TestHierarchy id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page, testHierarchy or statusResult not found")
    })
    @GetMapping("/by-page-and-test-hierarchy/{pageId}/{testHierarchyId}/{sharecode}")
    public @ResponseBody
    StatusResultDTO getByPageAndTestHierarchy(
            @PathVariable long pageId,
            @PathVariable long testHierarchyId,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {

        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot fine Page with id " + pageId));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit(), sharecode)){
            throw  new ForbiddenException("Current user cannot access to page result " + pageId);
        }

        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot fine test hierarchy with id " + testHierarchyId));


        return new StatusResultDTO(statusResultRepository.findByReferenceAndPage(
                testHierarchy,
                page
        ).orElseThrow(() -> new EntityNotFoundException("Cannot fine TestHierarchyResult for paget " + pageId + " and test hierarchy " + testHierarchyId)));
    }


    @ApiOperation(
            value = "Get the main StatusResult for a given Page id",
            notes = "The main result is the one associated with the main reference chosen for the audit. User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page or StatusResult not found")
    })
    @GetMapping("/main-result-by-page/{id}/{sharecode}")
    public @ResponseBody
    StatusResultDTO getMainStatusResultByPage(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find page with id " + id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit(), sharecode)){
            throw new ForbiddenException("Cannot show audit " + page.getAudit().getId());
        }

        return new StatusResultDTO(statusResultRepository.findByReferenceAndPage(
                auditReferenceRepository.findByAuditAndIsMainIsTrue(page.getAudit())
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find main reference for audit " + page.getAudit().getId()))
                        .getTestHierarchy(),
                page
                ).orElseThrow(() -> new EntityNotFoundException("Cannot find main hierarchy result for given page")));
    }

    @ApiOperation(
            value = "Get all main StatusResult for a given Audit id",
            notes = "The main result is the one associated with the main reference chosen for the audit. User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit or StatusResult not found")
    })
    @GetMapping("/main-result-by-audit/{id}/{sharecode}")
    public @ResponseBody
    Collection<StatusResultDTO> getMainTestHierarchyResultByAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit with id " + id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new ForbiddenException("Cannot show audit " + audit.getId());
        }

        return statusResultRepository.findAllByReferenceAndPage_Audit(
                auditReferenceRepository.findByAuditAndIsMainIsTrue(audit)
                        .orElseThrow(() -> new EntityNotFoundException("Cannot find main reference for audit " + audit.getId()))
                        .getTestHierarchy(),
                audit)
                .stream().map(StatusResultDTO::new)
                .collect(Collectors.toList());
    }
}
