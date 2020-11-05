package com.tanaguru.controller;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.entity.audit.Page;
import com.tanaguru.repository.PageRepository;
import com.tanaguru.service.TanaguruUserDetailsService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    @Autowired
    public PageController(PageRepository pageRepository, TanaguruUserDetailsService tanaguruUserDetailsService) {
        this.pageRepository = pageRepository;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
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
            @RequestParam(defaultValue = "10") int size) {
        if(tanaguruUserDetailsService.currentUserCanShowAudit(id, shareCode)){
            return pageRepository.findAllByAudit_Id(id, PageRequest.of(page, size));
        }else{
            throw new CustomForbiddenException(CustomError.CANNOT_ACCESS_PAGES_FOR_AUDIT, id );
        }
    }
}
