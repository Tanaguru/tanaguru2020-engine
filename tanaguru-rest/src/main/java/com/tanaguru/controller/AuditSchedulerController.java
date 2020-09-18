package com.tanaguru.controller;

import com.tanaguru.domain.dto.AuditSchedulerDTO;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.AuditScheduler;
import com.tanaguru.domain.exception.ForbiddenException;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.AuditSchedulerRepository;
import com.tanaguru.service.AuditSchedulerService;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
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

    @GetMapping("/{id}, {shareCode}")
    public @ResponseBody
    AuditScheduler getById(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode) {
        AuditScheduler auditScheduler = auditSchedulerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit scheduler " + id));
        if(!tanaguruUserDetailsService.currentUserCanShowAudit(id, shareCode)){
            throw new ForbiddenException("Current user cannot access audit scheduler " + id);
        }

        return auditScheduler;
    }

    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanScheduleOnAudit(#auditSchedulerDTO.getAuditId())")
    @PostMapping("/")
    public @ResponseBody
    AuditScheduler createAuditScheduler(@RequestBody @Valid AuditSchedulerDTO auditSchedulerDTO) {
        Audit audit = auditRepository.findById(auditSchedulerDTO.getAuditId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit " + auditSchedulerDTO.getAuditId()));
        return auditSchedulerService.createAuditScheduler(audit, auditSchedulerDTO.getTimer());
    }

    @PutMapping("/")
    public @ResponseBody
    AuditScheduler modifyAuditScheduler(@RequestBody @Valid AuditSchedulerDTO auditSchedulerDTO) {
        AuditScheduler auditScheduler = auditSchedulerRepository.findById(auditSchedulerDTO.getAuditSchedulerId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit scheduler " + auditSchedulerDTO.getAuditSchedulerId()));

        if(auditSchedulerDTO.getAuditId() != auditScheduler.getAudit().getId()){
            throw new InvalidEntityException("Cannot modify audit scheduler associated audit");
        }

        if(! auditSchedulerService.userCanScheduleOnAudit(
                tanaguruUserDetailsService.getCurrentUser(),
                auditScheduler.getAudit()
        )){
            throw new ForbiddenException("Cannot access audit scheduler " + auditScheduler.getId());
        }

        return auditSchedulerService.modifyAuditScheduler(auditScheduler, auditSchedulerDTO.getTimer(), auditScheduler.getLastExecution());
    }

    @DeleteMapping("/{id}")
    public @ResponseBody
    void deleteAuditScheduler(@PathVariable long id) {
        AuditScheduler auditScheduler = auditSchedulerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit scheduler " + id));
        if(! auditSchedulerService.userCanScheduleOnAudit(
                tanaguruUserDetailsService.getCurrentUser(),
                auditScheduler.getAudit()
        )){
            throw new ForbiddenException("Cannot access audit scheduler " + auditScheduler.getId());
        }
    }
}
