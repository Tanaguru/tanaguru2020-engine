package com.tanaguru.controller;

import java.util.Collection;
import java.util.Date;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.repository.ProjectRepository;
import com.tanaguru.repository.UserRepository;
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
    //@PreAuthorize(
            //"@tanaguruUserDetailsServiceImpl.currentUserCanShowAudit(#id, #shareCode)")
    @GetMapping(value = "/", produces = "application/json")
    String getStats(){
        return this.statsService.createStats().toString();
    }
    //Attention de gérer les droits d'accès
	
    
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
    //@PreAuthorize(
            //"@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    //"T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    //"#id)")
    @GetMapping("/nb-pages-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbPageByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbPageByPeriod(startDate, endDate);
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
    //@PreAuthorize(
            //"@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    //"T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    //"#id)")
    @GetMapping("/nb-site-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbSiteByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbSiteByPeriod(startDate, endDate);
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
    //@PreAuthorize(
            //"@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    //"T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    //"#id)")
    @GetMapping("/nb-scenario-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbScenarioByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbScenarioByPeriod(startDate, endDate);
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
    //@PreAuthorize(
            //"@tanaguruUserDetailsServiceImpl.currentUserHasAuthorityOnProject(" +
                    //"T(com.tanaguru.domain.constant.ProjectAuthorityName).SHOW_AUDIT, " +
                    //"#id)")
    @GetMapping("/nb-files-audited/{startDate}/{endDate}")
    public @ResponseBody
    Integer getNbFilesByPeriod(@PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date startDate, @PathVariable @DateTimeFormat(pattern="yyyy-MM-dd") Date endDate) {
        return this.statsService.getNbFileByPeriod(startDate, endDate);
    }
}
