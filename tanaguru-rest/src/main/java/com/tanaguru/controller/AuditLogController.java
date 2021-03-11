package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAuditLogLevel;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomInvalidArgumentException;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditLog;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.repository.AuditLogRepository;
import com.tanaguru.repository.AuditRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
                    + "\nIf audit not found, exception raise : AUDIT_NOT_FOUND with audit id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
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
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id ));
        return auditLogRepository.findAllByAudit(audit, PageRequest.of(page, size, Sort.by("date")));
    }
    
    /**
     * @param id The id of the @see Audit
     * @return A list of @see AuditLog
     */
    @ApiOperation(
            value = "Get paginable AuditLog for a given Audit id filtered by date and/or level",
            notes = "User must have SHOW_AUDIT authority on audit's project or a valid sharecode"
                    + "\nIf audit not found, exception raise : AUDIT_NOT_FOUND with audit id "
                    + "\nDate format must be : dd-MM-yyyy"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping("/by-audit-filtered/{id}/{shareCode}")
    public @ResponseBody
    Page<AuditLog> getAuditLogByAuditFiltered(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode,
            @RequestParam(defaultValue = "0") @ApiParam(required = false) int page,
            @RequestParam(defaultValue = "10") @ApiParam(required = false) int size,
            @RequestParam(required = false) @DateTimeFormat(pattern="dd-MM-yyyy") Optional<LocalDate> date,
            @RequestParam(required = false) @ApiParam(required = false) Optional<EAuditLogLevel> level) {
        
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id ));
        
        Page<AuditLog> logs = null;
        
        if(level.isPresent() && date.isPresent()) {
            LocalDate localDate = date.get();
            logs = auditLogRepository.findAllByAuditAndLevelAndDate(audit,level.get(), localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),PageRequest.of(page, size, Sort.by("date"))); 
        }else if(level.isPresent()) {
            logs = auditLogRepository.findAllByAuditAndLevel(audit,level.get(),PageRequest.of(page, size, Sort.by("date"))); 
        }else if(date.isPresent()) {
            LocalDate localDate = date.get();
            logs = auditLogRepository.findAllByAuditAndDate(audit, localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),PageRequest.of(page, size, Sort.by("date"))); 
        }else {
            logs = auditLogRepository.findAllByAudit(audit,PageRequest.of(page, size, Sort.by("date")));
        }

        return logs;
    }
    
    @ApiOperation(
            value = "Get the different log levels"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Not found")
    })
    @GetMapping("/levels")
    public @ResponseBody
    EAuditLogLevel[] getLevels() {
        return EAuditLogLevel.values();
    }
}
