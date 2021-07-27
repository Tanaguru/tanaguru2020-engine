package com.tanaguru.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
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
    
    @Value("${auditrunner.active}")
    private String[] browsers;

    private final BuildProperties buildProperties;

    public ConfigurationController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @ApiOperation(
            value = "Get session duration value",
            response = Long.class
    )
    @GetMapping("/session-duration")
    public @ResponseBody
    Long getSessionDuration(){
        return this.sessionDuration;
    }

    @ApiOperation(
            value = "Get browsers enabled",
            response = String.class
    )
    @GetMapping("/browsers-enabled")
    public @ResponseBody
    String[] getBrowsersEnabled(){
        return this.browsers;
    }
    
    @ApiOperation(
            value = "Get engine version",
            response = String.class
    )
    @GetMapping("/engine-version")
    public @ResponseBody
    String getEngineVersion(){
        return this.buildProperties.getVersion();
    }
    
}
