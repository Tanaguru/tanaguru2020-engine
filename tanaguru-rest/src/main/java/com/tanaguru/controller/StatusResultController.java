package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.dto.StatusResultDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.repository.*;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

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
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf page not found, exception raise : PAGE_NOT_FOUND with page id"
                    + "\nIf user cannot access page result, exception raise : USER_CANNOT_ACCESS_PAGE_RESULT with page id"
                    + "\nIf test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id"
                    + "\nIf cannot find test hierarchy for page, exception raise : CANNOT_FIND_TEST_HIERARCHY_FOR_PAGE with page id and test hierarchy id")
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, new long[] { pageId } ));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit(), sharecode)){
            throw  new CustomForbiddenException(CustomError.USER_CANNOT_ACCESS_PAGE_RESULT, new long[] { pageId } );
        }

        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, new long[] { testHierarchyId } ));


        return new StatusResultDTO(statusResultRepository.findByReferenceAndPage(
                testHierarchy,
                page
        ).orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_TEST_HIERARCHY_FOR_PAGE, new long[] { pageId , testHierarchyId } )));
    }


    @ApiOperation(
            value = "Get the main StatusResult for a given Page id",
            notes = "The main result is the one associated with the main reference chosen for the audit. User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf page not found, exception raise : PAGE_NOT_FOUND with page id"
                    + "\nIf cannot show audit, exception raise : CANNOT_SHOW_AUDIT with audit id"
                    + "\nIf cannot find main reference for audit, exception raise : CANNOT_FIND_MAIN_REFERENCE_AUDIT with audit id"
                    + "\nIf cannot find main hierarchy result for given page, exception raise : CANNOT_FIND_MAIN_HIERARCHY_RESULT_FOR_PAGE")
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, new long[] { id } ));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit(), sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, new long[] { page.getAudit().getId() } );
        }

        return new StatusResultDTO(statusResultRepository.findByReferenceAndPage(
                auditReferenceRepository.findByAuditAndIsMainIsTrue(page.getAudit())
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_MAIN_REFERENCE_AUDIT, new long[] { page.getAudit().getId() } ))
                        .getTestHierarchy(),
                page
                ).orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_MAIN_HIERARCHY_RESULT_FOR_PAGE)));
    }

    @ApiOperation(
            value = "Get all main StatusResult for a given Audit id",
            notes = "The main result is the one associated with the main reference chosen for the audit. User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf audit not found, exception raise : AUDIT_NOT_FOUND with audit id"
                    + "\nIf cannot find main reference for audit, exception raise : CANNOT_FIND_MAIN_REFERENCE_AUDIT with audit id")
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, new long[] { id } ));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, new long[] { audit.getId() } );
        }

        return statusResultRepository.findAllByReferenceAndPage_Audit(
                auditReferenceRepository.findByAuditAndIsMainIsTrue(audit)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_MAIN_REFERENCE_AUDIT, new long[] { audit.getId() } ))
                        .getTestHierarchy(),
                audit)
                .stream().map(StatusResultDTO::new)
                .collect(Collectors.toList());
    }
}
