package com.tanaguru.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tanaguru.service.StatsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author lpedrau
 */
@RestController
@RequestMapping("/stats")
public class StatsController {
	
	private final StatsService statsService;
	
	@Autowired
	public StatsController(StatsService statsService){
		this.statsService = statsService;
	}
	
	/**
     * Get all the stats in a json string
     */
    @ApiOperation(
            value = "Get a json string with all the stats",
            notes = "User must be super admin")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 403, message = "Forbidden for current session or invalid sharecode")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping(value = "/", produces = "application/json")
    String getStats(){
        return this.statsService.createStats().toString();
    }	
    
    /**
     * Get the number of pages audited over a period
     *
     * @param startDate the start date of the period
     * @param endDate the end date of the period
     * @return number of pages
     */
    @ApiOperation(
            value = "Get the number of pages audited over a period",
            notes = "User must be SUPER_ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping("/nb-page-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbPageAuditedByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbPageAuditedByPeriod(startDate, endDate);
    }
    
    /**
     * Get the number of website audited over a period
     *
     * @param startDate the start date of the period
     * @param endDate the end date of the period
     * @return number of website audited over a period
     */
    @ApiOperation(
            value = "Get the number of website audited over a period",
            notes = "User must be SUPER_ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping("/nb-site-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbSiteAuditedByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbSiteAuditedByPeriod(startDate, endDate);
    }
    
    /**
     * Get the number of scenario audited over a period
     *
     * @param startDate the start date of the period
     * @param endDate the end date of the period
     * @return number of scenario audited over a period
     */
    @ApiOperation(
            value = "Get the number of scenario audited over a period",
            notes = "User must be SUPER_ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping("/nb-scenario-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbScenarioAuditedByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbScenarioAuditedByPeriod(startDate, endDate);
    }
    
    /**
     * Get the number of files audited over a period
     *
     * @param startDate the start date of the period
     * @param endDate the end date of the period
     * @return number of files audited over a period
     */
    @ApiOperation(
            value = "Get the number of files audited over a period",
            notes = "User must be SUPER_ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message"),
            @ApiResponse(code = 403, message = "Forbidden for current session")
    })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping("/nb-file-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbFilesAuditedByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbFileAuditedByPeriod(startDate, endDate);
    }
}
