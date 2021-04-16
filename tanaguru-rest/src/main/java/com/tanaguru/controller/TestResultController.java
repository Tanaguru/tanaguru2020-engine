package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.repository.TestHierarchyResultRepository;
import com.tanaguru.repository.TestResultRepository;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/test-results")
public class TestResultController {

    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final TestResultRepository testResultRepository;
    private final PageRepository pageRepository;
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    private final TestHierarchyRepository testHierarchyRepository;

    @Autowired
    public TestResultController(TanaguruUserDetailsService tanaguruUserDetailsService, TestResultRepository testResultRepository, PageRepository pageRepository, TestHierarchyResultRepository testHierarchyResultRepository, TestHierarchyRepository testHierarchyRepository) {
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.testResultRepository = testResultRepository;
        this.pageRepository = pageRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.testHierarchyRepository = testHierarchyRepository;
    }

    /**
     * Get all @TestResult for a given page id
     *
     * @param id The @Page id
     * @return A collection of @see TestResult corresponding to the given @Page id
     */
    @ApiOperation(
            value = "Get all TestResult for a given Page id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf page not found, exception raise : PAGE_NOT_FOUND with page id"
                    + "\nIf user cannot access result page, exception raise : USER_CANNOT_ACCESS_PAGE_RESULT with page id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page not found : PAGE_NOT_FOUND error"
                    + "\nUser cannot access page result : USER_CANNOT_ACCESS_PAGE_RESULT error")
    })
    @GetMapping("/by-page/{id}/{shareCode}")
    public @ResponseBody
    Collection<TestResult> getAllTestResultByPage(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, id ));

        if(tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit().getId(), shareCode)){
            return testResultRepository.findAllByPage(page);
        }else{
            throw new CustomForbiddenException(CustomError.USER_CANNOT_ACCESS_PAGE_RESULT, id );
        }
    }

    /**
     * Get all @TestResult for a given @see TestHierarchyResult
     *
     * @param id The @TestHierarchyResult id
     * @return A collection of @see TestResult corresponding to the given @TestHierarchyResult id
     */
    @ApiOperation(
            value = "Get all TestResult for a given TestHierarchyResult id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf test hierarchy result not found, exception raise : TEST_HIERARCHY_RESULT_NOT_FOUND with test hierarchy result id"
                    + "\nIf cannot access result audit, exception raise : CANNOT_ACCESS_RESULT_AUDIT with audit id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Test hierarchy result not found : TEST_HIERARCHY_RESULT_NOT_FOUND error"
                    + "\nCannot access result audit : CANNOT_ACCESS_RESULT_AUDIT error")
    })
    @GetMapping("/by-test-hierarchy-result/{id}/{shareCode}")
    public @ResponseBody
    Collection<TestResult> getAllTestResultByTestHierarchyResult(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode) {

        TestHierarchyResult testHierarchyResult = testHierarchyResultRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_RESULT_NOT_FOUND, id ));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(testHierarchyResult.getPage().getAudit().getId(), shareCode)){
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_RESULT_AUDIT, testHierarchyResult.getPage().getAudit().getId() );
        }

        return testHierarchyResult.getTestResults();
    }

    @ApiOperation(
            value = "Get all TestResult for a given reference id and Page id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id"
                    + "\nIf page not found, exception raise : PAGE_NOT_FOUND with page id"
                    + "\nIf cannot access result audit, exception raise : CANNOT_ACCESS_RESULT_AUDIT with audit id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Test hierarchy not found : TEST_HIERARCHY_NOT_FOUND error"
                    + "\nPage not found : PAGE_NOT_FOUND error"
                    + "\nCannot access result audit : CANNOT_ACCESS_RESULT_AUDIT error")
    })
    @GetMapping("/by-reference-and-page/{referenceId}/{pageId}/{shareCode}")
    public @ResponseBody
    Collection<TestResult> getAllTestResultByReference(
            @PathVariable long referenceId,
            @PathVariable long pageId,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode) {

        TestHierarchy testHierarchy = testHierarchyRepository.findById(referenceId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, referenceId ));

        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, pageId ));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit().getId(), shareCode)){
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_RESULT_AUDIT, page.getAudit().getId() );
        }

        return testResultRepository.findDistinctByPageAndReferencesContaining(page, testHierarchy);
    }
}
