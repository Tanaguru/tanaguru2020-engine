package com.tanaguru.controller;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.entity.audit.PageContent;
import com.tanaguru.domain.exception.ForbiddenException;
import com.tanaguru.repository.AuditRepository;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.repository.PageContentRepository;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;

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

    @Autowired
    public PageContentController(PageContentRepository pageContentRepository, PageRepository pageRepository, TanaguruUserDetailsService tanaguruUserDetailsService, AuditRepository auditRepository, AuditService auditService) {
        this.pageContentRepository = pageContentRepository;
        this.pageRepository = pageRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.auditRepository = auditRepository;
        this.auditService = auditService;
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
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page not found")
    })
    @GetMapping("/by-page/{id}/{shareCode}")
    public @ResponseBody
    PageContent getPageContentByPage(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode) {
        Page page = pageRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        if(tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit().getId(), shareCode)){
            return page.getPageContent();
        }else{
            throw new ForbiddenException("Cannot access page content for page " + id);
        }
    }

    @ApiOperation(
            value = "Get first PageContent for a given Audit id",
            notes = "User must have SHOW_AUDIT authority on audit's project or a valid sharecode"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @GetMapping("/first-by-audit/{id}/{shareCode}")
    public @ResponseBody
    PageContent getFirstByAudit(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false)String shareCode) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit with id " + id));
        if(tanaguruUserDetailsService.currentUserCanShowAudit(audit.getId(), shareCode)){
            return pageContentRepository.findFirstByPage_Audit(audit).orElse(null);
        }else{
            throw new ForbiddenException("Cannot access page content for audit " + id);
        }
    }

    @ApiOperation(
            value = "Delete all screenshots for a given Audit id",
            notes = "User must have DELETE_AUDIT authority on audit's project"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session"),
            @ApiResponse(code = 404, message = "Audit not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanDeleteAudit(#id)")
    @PutMapping("/delete-screenshot-by-audit/{id}")
    public @ResponseBody
    void deleteScreenshotByAudit(
            @PathVariable long id) {
        Audit audit = auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find audit with id " + id));

        for(PageContent pageContent : pageContentRepository.findAllByPage_Audit(audit)){
            pageContent.setScreenshot(null);
            pageContentRepository.save(pageContent);
        }
    }
}
