package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.dto.AuditSchedulerDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.entity.audit.parameter.AuditParameter;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.AuditSchedulerRepository;
import com.tanaguru.service.AuditSchedulerService;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.TanaguruUserDetailsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/audit-schedulers")
public class AuditSchedulerController {
    private final AuditSchedulerService auditSchedulerService;
    private final AuditSchedulerRepository auditSchedulerRepository;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final AuditRepository auditRepository;
    private final AuditService auditService;

    public AuditSchedulerController(AuditSchedulerService auditSchedulerService, AuditSchedulerRepository auditSchedulerRepository, TanaguruUserDetailsService tanaguruUserDetailsService, AuditRepository auditRepository, AuditService auditService) {
        this.auditSchedulerService = auditSchedulerService;
        this.auditSchedulerRepository = auditSchedulerRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.auditRepository = auditRepository;
        this.auditService = auditService;
    }
    
    @ApiOperation(
            value = "Get audit scheduler by id",
            notes = "If audit scheduler not found, exception raise : AUDIT_SCHEDULER_NOT_FOUND with audit scheduler id"
                    + "\n Or if the current user doesn't have access to the scheduler, exception raise : CURRENT_USER_NO_ACCESS_SCHEDULER with audit scheduler id",
            response = AuditScheduler.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit scheduler not found : AUDIT_SCHEDULER_NOT_FOUND error"
                    + "\nCurrent user cannot access scheduler : CURRENT_USER_NO_ACCESS_SCHEDULER error")
    })
    @GetMapping("/{id}, {shareCode}")
    public @ResponseBody
    AuditScheduler getById(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode) {
        AuditScheduler auditScheduler = auditSchedulerRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_SCHEDULER_NOT_FOUND,id ));
        if(!tanaguruUserDetailsService.currentUserCanShowAudit(id, shareCode)){
            throw new CustomForbiddenException(CustomError.CURRENT_USER_NO_ACCESS_SCHEDULER, id );
        }

        return auditScheduler;
    }

    @ApiOperation(
            value = "Create audit scheduler",
            notes = "If audit not found, exception raise : AUDIT_NOT_FOUND with audit id",
            response = AuditScheduler.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit scheduler not found : AUDIT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanScheduleOnAudit(#auditSchedulerDTO.getAuditId())")
    @PostMapping("/")
    public @ResponseBody
    AuditScheduler createAuditScheduler(@RequestBody @Valid AuditSchedulerDTO auditSchedulerDTO) {
        Audit audit = auditRepository.findById(auditSchedulerDTO.getAuditId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, auditSchedulerDTO.getAuditId() ));
        return auditSchedulerService.createAuditScheduler(audit, auditSchedulerDTO.getTimer());
    }

    @ApiOperation(
            value = "Modify audit scheduler",
            notes = "If audit scheduler not found, exception raise : AUDIT_SCHEDULER_NOT_FOUND with audit scheduler id"
                    + "\n Or if user cannot modify audit associated with the scheduler, exception raise : CANNOT_MODIFY_AUDIT_ASSOCIATED_SCHEDULER"
                    + "\n Or if user cannot access scheduler, exception raise : CANNOT_ACCESS_SCHEDULER with audit scheduler id",
            response = AuditScheduler.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit scheduler not found : AUDIT_SCHEDULER_NOT_FOUND error"
                    + "\nCannot modify audit associated with the scheduler : CANNOT_MODIFY_AUDIT_ASSOCIATED_SCHEDULER error"
                    + "\nCannot access scheduler : CANNOT_ACCESS_SCHEDULER error")
    })
    @PutMapping("/")
    public @ResponseBody
    AuditScheduler modifyAuditScheduler(@RequestBody @Valid AuditSchedulerDTO auditSchedulerDTO) {
        AuditScheduler auditScheduler = auditSchedulerRepository.findById(auditSchedulerDTO.getAuditSchedulerId())
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_SCHEDULER_NOT_FOUND, auditSchedulerDTO.getAuditSchedulerId() ));

        if(auditSchedulerDTO.getAuditId() != auditScheduler.getAudit().getId()){
            throw new CustomInvalidEntityException(CustomError.CANNOT_MODIFY_AUDIT_ASSOCIATED_SCHEDULER);
        }

        if(! auditSchedulerService.userCanScheduleOnAudit(
                tanaguruUserDetailsService.getCurrentUser(),
                auditScheduler.getAudit()
        )){
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_SCHEDULER, auditScheduler.getId() );
        }

        return auditSchedulerService.modifyAuditScheduler(auditScheduler, auditSchedulerDTO.getTimer(), auditScheduler.getLastExecution());
    }
  
    @ApiOperation(
            value = "Delete audit scheduler",
            notes = "If audit scheduler not found, exception raise : AUDIT_SCHEDULER_NOT_FOUND with audit scheduler id"
                    + "\n Or if user cannot access scheduler, exception raise: CANNOT_ACCESS_SCHEDULER with audit scheduler id",
            response = AuditScheduler.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit scheduler not found : AUDIT_SCHEDULER_NOT_FOUND error"
                    + "\nCannot access scheduler : CANNOT_ACCESS_SCHEDULER error")
    })
    @DeleteMapping("/{id}")
    public @ResponseBody
    void deleteAuditScheduler(@PathVariable long id) {
        AuditScheduler auditScheduler = auditSchedulerRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_SCHEDULER_NOT_FOUND, id ));
        if(! auditSchedulerService.userCanScheduleOnAudit(
                tanaguruUserDetailsService.getCurrentUser(),
                auditScheduler.getAudit()
        )){
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_SCHEDULER, auditScheduler.getId() );
        }
    }
}
