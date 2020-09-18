package com.tanaguru.controller;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditLog;
import com.tanaguru.repository.AuditLogRepository;
import com.tanaguru.repository.AuditRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Collection;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditRepository auditRepository;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogController(AuditRepository auditRepository, AuditLogRepository auditLogRepository) {
        this.auditRepository = auditRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * @param id The id of the @see Audit
     * @return A list of @see AuditLog
     */
    @ApiOperation(
            value = "Get paginable AuditLog for a given Audit id",
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
    Page<AuditLog> getAuditLogByAudit(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode,
            @RequestParam(defaultValue = "0") @ApiParam(required = false) int page,
            @RequestParam(defaultValue = "10") @ApiParam(required = false) int size) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit with id " + id));
        return auditLogRepository.findAllByAudit(audit, PageRequest.of(page, size, Sort.by("date")));
    }
}
