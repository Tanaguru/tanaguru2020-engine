package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.exception.CustomInvalidArgumentException;
import com.tanaguru.domain.dto.TestHierarchyDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.TanaguruTestRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.service.TanaguruUserDetailsService;
import com.tanaguru.service.TestHierarchyService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/test-hierarchies")
public class TestHierarchyController {
    private final TestHierarchyRepository testHierarchyRepository;
    private final AuditRepository auditRepository;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final TanaguruTestRepository tanaguruTestRepository;
    private final TestHierarchyService testHierarchyService;

    @Autowired
    public TestHierarchyController(
            TestHierarchyRepository testHierarchyRepository, AuditRepository auditRepository, TanaguruUserDetailsService tanaguruUserDetailsService, TanaguruTestRepository tanaguruTestRepository, TestHierarchyService testHierarchyService) {
        this.testHierarchyRepository = testHierarchyRepository;
        this.auditRepository = auditRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.tanaguruTestRepository = tanaguruTestRepository;
        this.testHierarchyService = testHierarchyService;
    }

    /**
     * Return a @TestHierarchy for a given id
     *
     * @param id The id of the @see TestHierarchy
     * @return The @see TestHierarchy
     */
    @ApiOperation(
            value = "Get TestHierarchy for a given id",
            notes = "If test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "TestHierarchy not found : TEST_HIERARCHY_NOT_FOUND error")
    })
    @GetMapping("/{id}")
    public @ResponseBody
    TestHierarchyDTO getById(
            @PathVariable long id) {
        return new TestHierarchyDTO(testHierarchyRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, id)));
    }

    /**
     * Return a Collection of @TestHierarchy corresponding to references
     *
     * @return The Collection of @see TestHierarchy
     */
    @ApiOperation(
            value = "Get all TestHierarchy parent")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Reference not found")
    })
    @GetMapping("/references")
    public @ResponseBody
    Page<TestHierarchyDTO> getReferences(
            @RequestParam(defaultValue = "0") @ApiParam(required = false) int page,
            @RequestParam(defaultValue = "5") @ApiParam(required = false) int size) {
        return testHierarchyRepository.findAllByParentIsNullAndIsDeletedIsFalse(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")))
                .map(TestHierarchyDTO::new);
    }

    /**
     * Return a Reference for the given name
     *
     * @return Reference or null if not found
     */
    @ApiOperation(
            value = "Get a reference for a given name")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
    })
    @GetMapping("/reference-by-code/{code}")
    public @ResponseBody
    TestHierarchyDTO getReferenceByName(@PathVariable String code) {
        return testHierarchyRepository.findByCodeAndParentIsNull(code)
                .map(TestHierarchyDTO::new)
                .orElse(null);
    }

    /**
     * Return a Collection of @TestHierarchy corresponding to references that are not deleted
     *
     * @return The Collection of @see TestHierarchy that are not deleted
     */
    @ApiOperation(
            value = "Get all TestHierarchy for a given non deleted reference id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Reference not found")
    })
    @GetMapping("/references-not-deleted")
    public @ResponseBody
    Collection<TestHierarchyDTO> getNotDeletedReferences() {
        return testHierarchyRepository.findAllByParentIsNullAndIsDeletedIsFalse()
                .stream().map((TestHierarchyDTO::new)).collect(Collectors.toList());
    }

    /**
     * Return a collection of  @TestHierarchy for a given parent id
     *
     * @param id The id of the parent @see TestHierarchy
     * @return The collection of @see TestHierarchy
     */
    @ApiOperation(
            value = "Get all TestHierarchy children of a given TestHierarchy id",
            notes = "If test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "TestHierarchy not found : TEST_HIERARCHY_NOT_FOUND error")
    })
    @GetMapping("by-parent/{id}")
    public @ResponseBody
    Collection<TestHierarchyDTO> getByParentId(
            @PathVariable long id) {
        return testHierarchyRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, id))
                .getChildren().stream()
                .sorted(Comparator.comparingInt(TestHierarchy::getRank))
                .map(TestHierarchyDTO::new)
                .collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Get all TestHierarchy for a given TanaguruTest id and TestHierarchy id",
            notes = "If test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id"
                    + "\nIf tanaguru test not found, exception raise : TANAGURU_TEST_NOT_FOUND with tanaguru test id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "TestHierarchy not found : TEST_HIERARCHY_NOT_FOUND error"
                    + "\nTanaguru test not found : TANAGURU_TEST_NOT_FOUND error")
    })
    @GetMapping("by-test-and-reference/{testId}/{referenceId}")
    public @ResponseBody
    Collection<TestHierarchyDTO> getByTestAndReference(
            @PathVariable long testId,
            @PathVariable long referenceId) {
        TestHierarchy reference = testHierarchyRepository.findById(referenceId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, referenceId));

        TanaguruTest test = tanaguruTestRepository.findById(testId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TANAGURU_TEST_NOT_FOUND, testId));

        return testHierarchyRepository.findByReferenceAndTanaguruTestsContains(reference, test)
                .stream().map(TestHierarchyDTO::new).collect(Collectors.toList());
    }

    /**
     * Return a collection of @TestHierarchy for a given @see Audit id
     *
     * @param id The id of the @see Audit
     * @return The collection of @see TestHierarchy
     */
    @ApiOperation(
            value = "Get all references for a given audit id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode"
                    + "\nIf audit not found, exception raise : AUDIT_NOT_FOUND with audit id"
                    + "\nIf cannot show audit, exception raise : CANNOT_SHOW_AUDIT with audit id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error"
                    + "\nCannot show audit : CANNOT_SHOW_AUDIT error")
    })
    @GetMapping("/by-audit/{id}/{sharecode}")
    public @ResponseBody
    Collection<TestHierarchyDTO> getReferenceByAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id));

        if (!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)) {
            throw new CustomForbiddenException(CustomError.CANNOT_SHOW_AUDIT, audit.getId());
        }

        return audit.getAuditReferences().stream().map(
                (auditReference) -> new TestHierarchyDTO(auditReference.getTestHierarchy())
        ).collect(Collectors.toList());
    }

    /**
     * @param testHierarchyDTO The data to create the @see TestHierarchy
     * @return @see Resource
     */
    @ApiOperation(
            value = "Create a TestHierarchy",
            notes = "User must have CREATE_REFERENCE authority"
                    + "\nIf try to create multiples references, exception raise : CANNOT_CREATE_MULTIPLE_REFERENCES with test hierarchy code"
                    + "\nIf test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Test hierarchy not found : TEST_HIERARCHY_NOT_FOUND error"
                    + "\nCannot create multiple references : CANNOT_CREATE_MULTIPLE_REFERENCES error")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_REFERENCE)")
    @PostMapping("/")
    public @ResponseBody
    TestHierarchyDTO createTestHierarchy(@RequestBody @Valid TestHierarchyDTO testHierarchyDTO) {
        if (testHierarchyDTO.getParentId() != null && testHierarchyRepository.findByCodeAndParentIsNull(testHierarchyDTO.getCode()).isPresent()) {
            throw new CustomInvalidArgumentException(CustomError.CANNOT_CREATE_MULTIPLE_REFERENCES, testHierarchyDTO.getCode());
        }

        TestHierarchy testHierarchy = new TestHierarchy();
        testHierarchy.setCode(testHierarchyDTO.getCode());
        testHierarchy.setName(testHierarchyDTO.getName());
        testHierarchy.setRank(testHierarchyDTO.getRank());
        testHierarchy.setUrls(testHierarchyDTO.getUrls());

        testHierarchy.setReference(
                testHierarchyDTO.getReferenceId() == null ?
                        null :
                        testHierarchyRepository.findById(testHierarchyDTO.getReferenceId())
                                .orElseThrow(() -> new CustomInvalidArgumentException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyDTO.getReferenceId()))
        );

        testHierarchy.setParent(
                testHierarchyDTO.getParentId() == null ?
                        null :
                        testHierarchyRepository.findById(testHierarchyDTO.getParentId())
                                .orElseThrow(() -> new CustomInvalidArgumentException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyDTO.getParentId()))
        );

        return new TestHierarchyDTO(testHierarchyRepository.save(testHierarchy));
    }

    /**
     * @param id The id of the testHierarchy to delete
     */
    @ApiOperation(
            value = "Delete a TestHierarchy and children",
            notes = "User must have DELETE_REFERENCE authority"
                    + "\nIf test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "TestHierarchy not found : TEST_HIERARCHY_NOT_FOUND error")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).DELETE_REFERENCE)")
    @DeleteMapping("/reference/{id}")
    public @ResponseBody
    void deleteReference(@PathVariable long id) {
        TestHierarchy testHierarchy = testHierarchyRepository.findByIdAndIsDeletedIsFalseAndParentIsNull(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, id));

        testHierarchyService.deleteReference(testHierarchy);
    }

    /**
     * @param testHierarchyId The id of the @see TestHierarchy to modify
     * @param testId          The id of the @see TanaguruTest to add
     */
    @ApiOperation(
            value = "Add a TanaguruTest to a given TestHierarchy",
            notes = "User must have CREATE_REFERENCE authority"
                    + "\nIf test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id"
                    + "\nIf tanaguru test not found, exception raise : TANAGURU_TEST_NOT_FOUND with tanaguru test id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Test hierarchy not found : TEST_HIERARCHY_NOT_FOUND error"
                    + "\nTanaguru test not found : TANAGURU_TEST_NOT_FOUND error")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_REFERENCE)")
    @PutMapping("/{testHierarchyId}/add-test/{testId}")
    public @ResponseBody
    void addTestToTestHierarchy(@PathVariable long testHierarchyId, @PathVariable long testId) {
        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyId));

        TanaguruTest tanaguruTest = tanaguruTestRepository.findById(testId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TANAGURU_TEST_NOT_FOUND, testId));

        Collection<TanaguruTest> tanaguruTests = testHierarchy.getTanaguruTests();
        tanaguruTests.add(tanaguruTest);
        testHierarchyRepository.save(testHierarchy);
    }

    /**
     * @param testHierarchyId The id of the @see TestHierarchy to modify
     * @param tanaguruTestIds The id collection of @see TanaguruTest to add
     */
    @ApiOperation(
            value = "Add a TanaguruTest list to a given TestHierarchy",
            notes = "User must have CREATE_REFERENCE authority"
                    + "\nIf test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with test hierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Test hierarchy not found : TEST_HIERARCHY_NOT_FOUND error")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_REFERENCE)")
    @PutMapping("/{testHierarchyId}/add-test-list")
    public @ResponseBody
    void addTestListToTestHierarchy(@PathVariable long testHierarchyId, @RequestBody Collection<Long> tanaguruTestIds) {
        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, testHierarchyId));

        Collection<TanaguruTest> tanaguruTests = tanaguruTestRepository.findAllById(tanaguruTestIds);

        Collection<TanaguruTest> currentTanaguruTests = testHierarchy.getTanaguruTests();
        currentTanaguruTests.addAll(tanaguruTests);

        testHierarchy.setTanaguruTests(currentTanaguruTests);
        testHierarchyRepository.save(testHierarchy);
    }
}
