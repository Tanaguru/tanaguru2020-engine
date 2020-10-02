package com.tanaguru.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author rcharre
 */
@RestController
@RequestMapping("/config")
public class ConfigurationController {
    @Value("${session.duration}")
    private long sessionDuration;

    @ApiOperation(
            value = "Get session duration value",
            response = Long.class
    )
    @GetMapping("/session-duration")
    public @ResponseBody
    Long getSessionDuration(){
        return this.sessionDuration;
    }

}
