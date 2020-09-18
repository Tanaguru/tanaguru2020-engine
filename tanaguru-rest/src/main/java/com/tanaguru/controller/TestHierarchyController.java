package com.tanaguru.controller;

import com.tanaguru.domain.dto.TestHierarchyDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.exception.ForbiddenException;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.TanaguruTestRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import com.tanaguru.service.TanaguruUserDetailsService;
import com.tanaguru.service.TestHierarchyService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.openqa.selenium.InvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Comparator;
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
     * @param id The id of the @see TestHierarchy
     * @return The @see TestHierarchy
     */
    @ApiOperation(
            value = "Get TestHierarchy for a given id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "TestHierarchy not found")
    })
    @GetMapping("/{id}")
    public @ResponseBody
    TestHierarchyDTO getById(
            @PathVariable long id) {
        return new TestHierarchyDTO(testHierarchyRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new));
    }

    /**
     * Return a Collection of @TestHierarchy corresponding to references
     * @return The Collection of @see TestHierarchy
     */
    @ApiOperation(
            value = "Get all TestHierarchy for a given reference id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Reference not found")
    })
    @GetMapping("/references")
    public @ResponseBody
    Collection<TestHierarchyDTO> getReferences() {
        return testHierarchyRepository.findAllByParentIsNull()
                .stream().map((TestHierarchyDTO::new)).collect(Collectors.toList());
    }

    /**
     * Return a Collection of @TestHierarchy corresponding to references that are not deleted
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
     * @param id The id of the parent @see TestHierarchy
     * @return The collection of @see TestHierarchy
     */
    @ApiOperation(
            value = "Get all TestHierarchy children of a given TestHierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "TestHierarchy not found")
    })
    @GetMapping("by-parent/{id}")
    public @ResponseBody
    Collection<TestHierarchyDTO> getByParentId(
            @PathVariable long id) {
        return testHierarchyRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new)
                .getChildren().stream()
                .sorted(Comparator.comparingInt(TestHierarchy::getRank))
                .map(TestHierarchyDTO::new)
                .collect(Collectors.toList());
    }

    @ApiOperation(
            value = "Get all TestHierarchy for a given TanaguruTest id and TestHierarchy id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "TanaguruTest or TestHierarchy not found")
    })
    @GetMapping("by-test-and-reference/{testId}/{referenceId}")
    public @ResponseBody
    Collection<TestHierarchyDTO> getByTestAndReference(
            @PathVariable long testId,
            @PathVariable long referenceId) {
        TestHierarchy reference = testHierarchyRepository.findById(referenceId)
                .orElseThrow(EntityNotFoundException::new);

        TanaguruTest test = tanaguruTestRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find Tanaguru test with id " + testId));

        return testHierarchyRepository.findByReferenceAndTanaguruTestsContains(reference, test)
                .stream().map(TestHierarchyDTO::new).collect(Collectors.toList());
    }

    /**
     * Return a collection of @TestHierarchy for a given @see Audit id
     * @param id The id of the @see Audit
     * @return The collection of @see TestHierarchy
     */
    @ApiOperation(
            value = "Get all references for a given audit id",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @GetMapping("/by-audit/{id}/{sharecode}")
    public @ResponseBody
    Collection<TestHierarchyDTO> getReferenceByAudit(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String sharecode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit with id " + id));

        if(!tanaguruUserDetailsService.currentUserCanShowAudit(audit, sharecode)){
            throw new ForbiddenException("Cannot show audit " + audit.getId());
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
            notes = "User must have CREATE_REFERENCE authority")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_REFERENCE)")
    @PostMapping("/")
    public @ResponseBody
    TestHierarchyDTO createTestHierarchy(@RequestBody @Valid TestHierarchyDTO testHierarchyDTO) {
        if(testHierarchyDTO.getParentId() != null && testHierarchyRepository.findByCodeAndParentIsNull(testHierarchyDTO.getCode()).isPresent()){
            throw new InvalidArgumentException("Cannot create multiple references with same code " + testHierarchyDTO.getCode());
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
                                .orElseThrow(() -> new InvalidArgumentException("Cannot find test Hierarchy with id " + testHierarchyDTO.getReferenceId()))
        );

        testHierarchy.setParent(
                testHierarchyDTO.getParentId() == null ?
                        null :
                        testHierarchyRepository.findById(testHierarchyDTO.getParentId())
                                .orElseThrow(() -> new InvalidArgumentException("Cannot find test Hierarchy with id " + testHierarchyDTO.getParentId()))
                );

        return new TestHierarchyDTO(testHierarchyRepository.save(testHierarchy));
    }

    /**
     * @param id The id of the testHierarchy to delete
     */
    @ApiOperation(
            value = "Delete a TestHierarchy and children",
            notes = "User must have DELETE_REFERENCE authority")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "TestHierarchy not found")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).DELETE_REFERENCE)")
    @DeleteMapping("/reference/{id}")
    public @ResponseBody
    void deleteReference(@PathVariable long id) {
        TestHierarchy testHierarchy = testHierarchyRepository.findByIdAndIsDeletedIsFalseAndParentIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find reference with id " + id));

        testHierarchyService.deleteReference(testHierarchy);
    }

    /**
     * @param testHierarchyId The id of the @see TestHierarchy to modify
     * @param testId The id of the @see TanaguruTest to add
     */
    @ApiOperation(
            value = "Add a TanaguruTest to a given TestHierarchy",
            notes = "User must have CREATE_REFERENCE authority")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "TestHierarchy or Tanaguru not found")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_REFERENCE)")
    @PutMapping("/{testHierarchyId}/add-test/{testId}")
    public @ResponseBody
    void addTestToTestHierarchy(@PathVariable long testHierarchyId, @PathVariable long testId) {
        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find testHierarchy with id " + testHierarchyId));

        TanaguruTest tanaguruTest = tanaguruTestRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find test with id " + testId));

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
            notes = "User must have CREATE_REFERENCE authority")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "TestHierarchy or Tanaguru not found")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_REFERENCE)")
    @PutMapping("/{testHierarchyId}/add-test-list")
    public @ResponseBody
    void addTestListToTestHierarchy(@PathVariable long testHierarchyId, @RequestBody Collection<Long> tanaguruTestIds) {
        TestHierarchy testHierarchy = testHierarchyRepository.findById(testHierarchyId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find testHierarchy with id " + testHierarchyId));

        Collection<TanaguruTest> tanaguruTests = tanaguruTestRepository.findAllById(tanaguruTestIds);

        Collection<TanaguruTest> currentTanaguruTests = testHierarchy.getTanaguruTests();
        currentTanaguruTests.addAll(tanaguruTests);

        testHierarchy.setTanaguruTests(currentTanaguruTests);
        testHierarchyRepository.save(testHierarchy);
    }
}
