package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.helper.JsonHttpHeaderBuilder;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.service.PageService;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/pages")
public class PageController {

    private final PageRepository pageRepository;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final PageService pageService;

    @Autowired
    public PageController(PageRepository pageRepository, TanaguruUserDetailsService tanaguruUserDetailsService, PageService pageService) {
        this.pageRepository = pageRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.pageService = pageService;
    }

    /**
     * @param id The @see Page id
     * @param shareCode the share code of the @see Audit
     * @return The corresponding @see Page
     */
    @ApiOperation(
            value = "Get Page for a given id",
            notes = "User must have SHOW_AUDIT authority on page's project or a valid sharecode"
                    + "\nIf page not found, exception raise : PAGE_NOT_FOUND with page id"
                    + "\nIf user cannot access page content, exception raise : CANNOT_ACCESS_PAGE_CONTENT_FOR_PAGE with page id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page not found : PAGE_NOT_FOUND error"
                    + "\nCannot access page content for page : CANNOT_ACCESS_PAGE_CONTENT_FOR_PAGE error")
    })
    @GetMapping("/{id}/{shareCode}")
    public @ResponseBody
    Page getPage(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, id ));

        if(tanaguruUserDetailsService.currentUserCanShowAudit(page.getAudit().getId(), shareCode)){
            return page;
        }else{
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_PAGE_CONTENT_FOR_PAGE, id );
        }
    }

    /**
     * Get all @sse Page for a given @see Audit id
     *
     * @param id The @see Audit id
     * @param shareCode the share code of the @see Audit
     * @return A @see Page
     */
    @ApiOperation(
            value = "Get all Page for a given Audit id",
            notes = "User must have SHOW_AUDIT authority on audit's project or a valid sharecode"
                    + "\nIf user cannot access pages for the audit, exception raise : CANNOT_ACCESS_PAGES_FOR_AUDIT with audit id"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Cannot access pages for audit : CANNOT_ACCESS_PAGES_FOR_AUDIT error")
    })
    @GetMapping("/by-audit/{id}/{shareCode}")
    public @ResponseBody
    Collection<Page> getPagesByAudit(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode) {
        if(tanaguruUserDetailsService.currentUserCanShowAudit(id, shareCode)){
            return pageRepository.findAllByAudit_Id(id);
        }else{
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_PAGES_FOR_AUDIT, id );
        }
    }

    /**
     * Get all @sse Page for a given @see Audit id
     *
     * @param id The @see Audit id
     * @param shareCode the share code of the @see Audit
     * @return A @see Page
     */
    @ApiOperation(
            value = "Get paginated Page for a given Audit id",
            notes = "User must have SHOW_AUDIT authority on audit's project or a valid sharecode"
                    + "\nIf user cannot access pages for the audit, exception raise : CANNOT_ACCESS_PAGES_FOR_AUDIT"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Cannot access pages for audit : CANNOT_ACCESS_PAGES_FOR_AUDIT error")
    })
    @GetMapping("/by-audit-paginated/{id}/{shareCode}")
    public @ResponseBody
    org.springframework.data.domain.Page<Page> getPaginatedPagesByAudit(
            @PathVariable long id,
            @PathVariable(required = false) @ApiParam(required = false) String shareCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "true") boolean isAsc,
            @RequestParam(defaultValue = "") String name) {
        if(tanaguruUserDetailsService.currentUserCanShowAudit(id, shareCode)){
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(isAsc ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy));
            return pageRepository.findByNameContainingIgnoreCaseAndAudit_Id(name, id, pageRequest);
        }else{
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_PAGES_FOR_AUDIT, id );
        }
    }
    
    /**
     * Get a json file with the page information
     * @param id The id of the @see Page
     * @param shareCode the share code of the @see Audit
     */
    @ApiOperation(
            value = "Get a json file with the page information",
            notes = "User must have SHOW_AUDIT authority on project or a valid sharecode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode"),
            @ApiResponse(code = 404, message = "Page not found")
    })
    @PreAuthorize(
            "@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping(value="/export/{id}/{sharecode}", produces = "application/json")
    public ResponseEntity<Resource> exportPage(
            @PathVariable long id,
            @ApiParam(required = false) @PathVariable(required = false) String shareCode) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PAGE_NOT_FOUND, id ));
        JSONObject jsonFinalObject = pageService.toJsonWithAuditInfo(page);
        byte[] buf = jsonFinalObject.toString().getBytes();              
        HttpHeaders header = JsonHttpHeaderBuilder.setUpJsonHeaders(page.getName(), "json");
        return ResponseEntity
                .ok()
                .headers(header)
                .contentLength(buf.length)
                .contentType(MediaType.parseMediaType("application/json"))
                .body(new ByteArrayResource(buf));
    }
}
