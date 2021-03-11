package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAuditParameter;
import com.tanaguru.domain.entity.audit.parameter.AuditParameterValue;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.PageContent;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.repository.PageContentRepository;
import com.tanaguru.service.AuditParameterService;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/page-contents")
public class PageContentController {

    private final PageContentRepository pageContentRepository;
    private final PageRepository pageRepository;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final AuditRepository auditRepository;
    private final AuditService auditService;
    private final AuditParameterService auditParameterService;

    @Autowired
    public PageContentController(PageContentRepository pageContentRepository, PageRepository pageRepository, TanaguruUserDetailsService tanaguruUserDetailsService, AuditRepository auditRepository, AuditService auditService, AuditParameterService auditParameterService) {
        this.pageContentRepository = pageContentRepository;
        this.pageRepository = pageRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.auditRepository = auditRepository;
        this.auditService = auditService;
        this.auditParameterService = auditParameterService;
    }
    /**
     * Get the @PageContent for a given page id
     *
     * @param id The @Page id
     * @return A @see PageContent corresponding to the given @Page id
     */
    @ApiOperation(
            value = "Get PageContent for a given Page id",
            notes = "User must have SHOW_AUDIT authority on page's project or a valid sharecode"
                    + "\nIf page not found, exception raise : PAGE_NOT_FOUND with page id"
                    + "\nIf user cannot access page content, exception raise : CANNOT_ACCESS_PAGE_CONTENT_FOR_PAGE with page id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page not found : PAGE_NOT_FOUND error"
                    + "\nCannot access page content : CANNOT_ACCESS_PAGE_CONTENT_FOR_PAGE error")
    })
    @GetMapping("/by-page/{id}/{shareCode}")
    public @ResponseBody
    PageContent getPageContentByPage(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, id ));

        if(tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit().getId(), shareCode)){
            return page.getPageContent();
        }else{
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_PAGE_CONTENT_FOR_PAGE, id );
        }
    }

    @ApiOperation(
            value = "Get first PageContent for a given Audit id",
            notes = "User must have SHOW_AUDIT authority on audit's project or a valid sharecode"
                    + "\nIf audit not found exception raise : AUDIT_NOT_FOUND with audit id"
                    + "\nIf user cannot access page content for the audit, exception raise : CANNOT_ACCESS_PAGE_CONTENT_FOR_AUDIT with audit id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error"
                    + "\nCannot access page content for audit : CANNOT_ACCESS_PAGE_CONTENT_FOR_AUDIT error")
    })
    @GetMapping("/first-by-audit/{id}/{shareCode}")
    public @ResponseBody
    PageContent getFirstByAudit(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id ));
        if(tanaguruUserDetailsService.currentUserCanShowAudit(audit.getId(), shareCode)){
            return pageContentRepository.findFirstByPage_Audit(audit).orElse(null);
        }else{
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_PAGE_CONTENT_FOR_AUDIT, id );
        }
    }

    @ApiOperation(
            value = "Delete all screenshots for a given Audit id",
            notes = "User must have DELETE_AUDIT authority on audit's project"
                    + "\nIf audit not found, exception raise : AUDIT_NOT_FOUND with audit id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit not found : AUDIT_NOT_FOUND error")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanDeleteAudit(#id)")
    @PutMapping("/delete-screenshot-by-audit/{id}")
    public @ResponseBody
    void deleteScreenshotByAudit(
            @PathVariable long id) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.AUDIT_NOT_FOUND, id ));

        auditParameterService.modifyAuditParameterValue(audit, EAuditParameter.ENABLE_SCREENSHOT, "false");

        for(PageContent pageContent : pageContentRepository.findAllByPage_Audit(audit)){
            pageContent.setScreenshot(null);
            pageContentRepository.save(pageContent);
        }
    }
}
