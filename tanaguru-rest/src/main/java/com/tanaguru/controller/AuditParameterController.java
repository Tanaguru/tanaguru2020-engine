package com.tanaguru.controller;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.repository.AuditParameterRepository;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.service.AuditParameterService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/audit-parameters")
public class AuditParameterController {
    private final AuditParameterRepository auditParameterRepository;
    private final AuditParameterService auditParameterService;
    private final AuditRepository auditRepository;

    @Autowired
    public AuditParameterController(AuditParameterRepository auditParameterRepository, AuditParameterService auditParameterService, AuditRepository auditRepository) {
        this.auditParameterRepository = auditParameterRepository;
        this.auditParameterService = auditParameterService;
        this.auditRepository = auditRepository;
    }

    /**
     * @return A @see Collection containing all @see AuditParameters
     */
    @ApiOperation(
            value = "Get all parameters"
    )
    @GetMapping("/")
    public @ResponseBody
    Collection<AuditParameter> getAllAuditParameters() {
        return auditParameterRepository.findAll();
    }

    /**
     * Get an @see AuditParameters @see Collection for a given @see Audit id
     *
     * @param id The id of the @see Audit
     * @return An @see AuditParameters @see Collection
     */
    @ApiOperation(
            value = "Get parameter by id",
            response = AuditParameter.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 404, message = "Parameter not found")
    })
    @GetMapping("/{id}")
    public @ResponseBody
    AuditParameter getAuditParameters(@PathVariable long id) {
        return auditParameterRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @ApiOperation(
            value = "Get parameters for a given Audit id",
            notes = "User must have SHOW_AUDIT authority on audit's project or a valid sharecode"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping("/by-audit/{id}/{shareCode}")
    public @ResponseBody
    Collection<AuditParameterValue> getByAudit(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return audit.getParametersAsMap().values();
    }

}
