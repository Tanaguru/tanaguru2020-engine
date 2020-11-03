package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.dto.AuditSynthesisDTO;
import com.tanaguru.domain.dto.TestHierarchyResultDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.entity.pageresult.TestHierarchyResult;
import com.tanaguru.repository.*;
import com.tanaguru.service.TanaguruUserDetailsService;
import com.tanaguru.service.TestHierarchyResultService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/test-hierarchy-results")
public class TestHierarchyResultController {
    private final TestHierarchyRepository testHierarchyRepository;
    private final TestHierarchyResultRepository testHierarchyResultRepository;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final PageRepository pageRepository;
    private final AuditRepository auditRepository;
    private final AuditReferenceRepository auditReferenceRepository;
    private final TestHierarchyResultService testHierarchyResultService;

    @Autowired
    public TestHierarchyResultController(
            TestHierarchyRepository testHierarchyRepository,
            TestHierarchyResultRepository testHierarchyResultRepository,
            TanaguruUserDetailsService tanaguruUserDetailsService,
            PageRepository pageRepository,
            AuditRepository auditRepository,
            AuditReferenceRepository auditReferenceRepository,
            TestHierarchyResultService testHierarchyResultService) {
        this.testHierarchyRepository = testHierarchyRepository;
        this.testHierarchyResultRepository = testHierarchyResultRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.pageRepository = pageRepository;
        this.auditRepository = auditRepository;
        this.auditReferenceRepository = auditReferenceRepository;
        this.testHierarchyResultService = testHierarchyResultService;
    }

    /**
     * Return a @TestHierarchyResult for a given @see Page and @see TestHierarchy
     * @param pageId The id of the @see Page
     * @param testHierarchyId The id of the @see TestHierarchy
     * @param sharecode The @see Audit sharecode
     * @return A @see TestHierarchyResult
     */
    @ApiOperation(
            value = "Get a TestHierarchyResult for a given Page id and TestHierarchy id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page, TestHierarchy of TestHierarchyResult not found")
    })
    @GetMapping("/by-page-and-test-hierarchy/{pageId}/{testHierarchyId}/{sharecode}")
    public @ResponseBody
    TestHierarchyResultDTO getByPageAndTestHierarchy(
            @PathVariable long pageId,
            @PathVariable long testHierarchyId,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {

        Page page = pageRepository.findById(pageId)
            .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, pageId));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit(), sharecode)){
            throw  new CustomForbiddenException(CustomError.USER_CANNOT_ACCESS_PAGE_RESULT, pageId);
        }

        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
            .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyId));


        return new TestHierarchyResultDTO(testHierarchyResultRepository.findByTestHierarchyAndPage(
            testHierarchy,
            page
        ).orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_TEST_HIERARCHY_FOR_PAGE, pageId + "," + testHierarchyId)));
    }

    @ApiOperation(
            value = "Get all TestHierarchyResult for a given parent TestHierarchyResult id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "TestHierarchyResult parent not found")
    })
    @GetMapping("/by-parent/{id}/{sharecode}")
    public @ResponseBody
    Collection<TestHierarchyResultDTO> getByParent(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {

        TestHierarchyResult parent = testHierarchyResultRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_RESULT_NOT_FOUND, id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(parent.getPage().getAudit(), sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, parent.getPage().getAudit().getId());
        }

        return parent.getChildren().stream()
                .sorted(Comparator.comparingInt(e -> e.getTestHierarchy().getRank()))
                .map(TestHierarchyResultDTO::new)
                .collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Get the main TestHierarchyResult for a given Page id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page or TestHierarchyResult not found")
    })
    @GetMapping("/main-result-by-page/{id}/{sharecode}")
    public @ResponseBody
    TestHierarchyResultDTO getMainTestHierarchyResultByPage(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit(), sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, page.getAudit().getId());
        }

        return new TestHierarchyResultDTO(
                testHierarchyResultRepository.findByTestHierarchyAndPage(
                        auditReferenceRepository.findByAuditAndIsMainIsTrue(page.getAudit())
                                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_MAIN_REFERENCE_AUDIT, page.getAudit().getId()))
                                .getTestHierarchy(),
                        page
                ).orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_MAIN_HIERARCHY_RESULT_FOR_PAGE)));
    }

    @ApiOperation(
            value = "Get all main TestHierarchyResult for a given Audit id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @GetMapping("/main-result-by-audit/{id}/{sharecode}")
    public @ResponseBody
    Collection<TestHierarchyResultDTO> getMainTestHierarchyResultByAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, audit.getId());
        }

        return testHierarchyResultRepository.findAllByPage_AuditAndTestHierarchy(
                audit,
                auditReferenceRepository.findByAuditAndIsMainIsTrue(audit)
                        .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.CANNOT_FIND_MAIN_REFERENCE_AUDIT, audit.getId()))
                        .getTestHierarchy())
                .stream().map(TestHierarchyResultDTO::new)
                .collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Get all TestHierarchyResult for a given Audit id and TestHierarchy id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit or TestHierarchy not found")
    })
    @GetMapping("/test-hierarchy-result-by-audit/{testHierarchyId}/{id}/{sharecode}")
    public @ResponseBody
    Collection<TestHierarchyResultDTO> getTestHierarchyResultByAudit(
            @PathVariable long testHierarchyId,
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, audit.getId());
        }

        return testHierarchyResultRepository.findAllByPage_AuditAndTestHierarchy(
                audit,
                testHierarchyRepository.findById(testHierarchyId)
                    .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyId)))
                .stream().map(TestHierarchyResultDTO::new)
                .collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Get a reduced TestHierarchyResult for a given Audit id and TestHierarchy id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit or TestHierarchy not found")
    })
    @GetMapping("/reduced-test-hierarchy-result-by-audit/{testHierarchyId}/{id}/{sharecode}")
    public @ResponseBody
    TestHierarchyResultDTO getReducedTestHierarchyResultByAudit(
            @PathVariable long testHierarchyId,
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, audit.getId());
        }

        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyId));

        return testHierarchyResultService.getReducedResultByAudit(audit, testHierarchy);
    }

    @ApiOperation(
            value = "Get all reduced children TestHierarchyResult for a given Audit id and TestHierarchy id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit or TestHierarchy not found")
    })
    @GetMapping("/reduced-children-test-hierarchy-result-by-audit/{testHierarchyId}/{id}/{sharecode}")
    public @ResponseBody
    Collection<TestHierarchyResultDTO> getReducedChildrenTestHierarchyResultByAudit(
            @PathVariable long testHierarchyId,
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, audit.getId());
        }

        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyId));

        Collection<TestHierarchyResultDTO> result = new ArrayList<>();
        for(TestHierarchy child : testHierarchy.getChildren()){
            result.add(testHierarchyResultService.getReducedResultByAudit(audit, child));
        }
        return result;
    }

    @ApiOperation(
            value = "Get a paginated audit synthesis for a given Audit id and reference id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode. Warning, this function is resource heavy")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit or reference not found")
    })
    @GetMapping("/synthesis-by-audit-and-test-hierarchy/{auditId}/{referenceId}/{sharecode}")
    public @ResponseBody
    AuditSynthesisDTO getSynthesisByAuditAndTestHierarchy(
            @PathVariable long auditId,
            @PathVariable long referenceId,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, auditId));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, audit.getId());
        }

        TestHierarchy testHierarchy = testHierarchyRepository.findById(referenceId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, referenceId));

        return testHierarchyResultService.getAuditSynthesisForTestHierarchy(audit, testHierarchy, PageRequest.of(page, size));
    }
}
