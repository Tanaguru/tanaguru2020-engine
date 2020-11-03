package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.entity.pageresult.ElementResult;
import com.tanaguru.domain.entity.pageresult.TestResult;
import com.tanaguru.repository.ElementResultRepository;
import com.tanaguru.repository.TestResultRepository;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/element-results")
public class ElementResultController {
    private final TestResultRepository testResultRepository;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final ElementResultRepository elementResultRepository;

    @Autowired
    public ElementResultController(TestResultRepository testResultRepository, TanaguruUserDetailsService tanaguruUserDetailsService, ElementResultRepository elementResultRepository) {
        this.testResultRepository = testResultRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.elementResultRepository = elementResultRepository;
    }


    /**
     * Get all @see ElementResult for a given @see TestResult id
     *
     * @param id The @TestResult id
     * @return A page of @see ElementResult
     */
    @ApiOperation(
            value = "Get all ElementResult for a given TestResult id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "TestResult not found")
    })
    @GetMapping("/by-test-result/{id}/{shareCode}")
    public @ResponseBody
    Page<ElementResult> getAllElementResultByTestResult(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        TestResult testResult = testResultRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.TEST_RESULT_NOT_FOUND, id));

        if(tanaguruUserDetailsService.currentUserCanShowAudit(testResult.getPage().getAudit().getId(), shareCode)){
            return elementResultRepository.findAllByTestResult(testResult, PageRequest.of(page, size, Sort.by("id")));
        }else{
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_ELEMENT_RESULTS_FOR_TEST, id);
        }
    }
}
