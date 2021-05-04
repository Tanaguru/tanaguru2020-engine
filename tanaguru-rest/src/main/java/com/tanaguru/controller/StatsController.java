package com.tanaguru.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
	
}
