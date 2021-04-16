package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.dto.TanaguruTestDTO;
import com.tanaguru.domain.entity.audit.TanaguruTest;
import com.tanaguru.domain.entity.audit.TestHierarchy;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.repository.TanaguruTestRepository;
import com.tanaguru.repository.TestHierarchyRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/tanaguru-tests")
public class TanaguruTestController {
    private final TanaguruTestRepository tanaguruTestRepository;
    private final TestHierarchyRepository testHierarchyRepository;

    public TanaguruTestController(TanaguruTestRepository tanaguruTestRepository, TestHierarchyRepository testHierarchyRepository) {
        this.tanaguruTestRepository = tanaguruTestRepository;
        this.testHierarchyRepository = testHierarchyRepository;
    }

    /**
     * Return a @see TanaguruTest for a given id
     * @param id The id of the @see TanaguruTest
     * @return The @see TanaguruTest
     */
    @ApiOperation(
            value = "Get TanaguruTest for a given id",
            notes = "If tanaguru test not found, exception raise : TANAGURU_TEST_NOT_FOUND with tanaguru test id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "TanaguruTest not found : TANAGURU_TEST_NOT_FOUND error")
    })
    @GetMapping("/{id}")
    public @ResponseBody
    TanaguruTestDTO getById(
            @PathVariable long id) {
        return new TanaguruTestDTO(
                tanaguruTestRepository.findById(id)
                    .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TANAGURU_TEST_NOT_FOUND, id )));
    }

    /**
     * Return a collection of @see TanaguruTest for a given reference id
     * @param id The id of the @see TestHierarchy
     * @return The collection of @see TanaguruTest
     */
    @ApiOperation(
            value = "Get all TanaguruTest for a given reference id",
            notes = "If test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Test hierarchy not found : TEST_HIERARCHY_NOT_FOUND error")
    })
    @GetMapping("/by-reference/{id}")
    public @ResponseBody
    Collection<TanaguruTestDTO> getByReferenceId(
            @PathVariable long id) {
        TestHierarchy reference = testHierarchyRepository.findByIdAndIsDeletedIsFalseAndParentIsNull(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, id ));

        return tanaguruTestRepository.findAllByTestHierarchies_ReferenceAndIsDeletedIsFalse(reference)
                .stream()
                .map(TanaguruTestDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Return a collection of @see TanaguruTest for a given @see TestHierarchy id
     * @param id The id of the @see TestHierarchy
     * @return The collection of @see TanaguruTest
     */
    @ApiOperation(
            value = "Get all TanaguruTest for a given TestHierarchy id",
            notes = "If test hierarchy not found, exception raise : TEST_HIERARCHY_NOT_FOUND with id")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Test hierarchy not found : TEST_HIERARCHY_NOT_FOUND error")
    })
    @GetMapping("/by-test-hierarchy/{id}")
    public @ResponseBody
    Collection<TanaguruTestDTO> getByTestHierarchyId(
            @PathVariable long id) {
        TestHierarchy testHierarchy = testHierarchyRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_HIERARCHY_NOT_FOUND, id ));

        return tanaguruTestRepository.findAllByTestHierarchiesContainsAndIsDeletedIsFalse(testHierarchy)
                .stream()
                .map(TanaguruTestDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * @param tanaguruTest The data to create the @see Resource
     * @return @see Resource
     */
    @ApiOperation(
            value = "Create a TanaguruTest",
            notes = "User must have CREATE_TEST authority")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session")
    })
    @PreAuthorize(
            "hasAuthority(T(com.tanaguru.domain.constant.AppAuthorityName).CREATE_TEST)")
    @PostMapping("/")
    public @ResponseBody
    TanaguruTestDTO createTanaguruTest(@RequestBody TanaguruTest tanaguruTest) {
        return new TanaguruTestDTO(tanaguruTestRepository.save(tanaguruTest));
    }
}
