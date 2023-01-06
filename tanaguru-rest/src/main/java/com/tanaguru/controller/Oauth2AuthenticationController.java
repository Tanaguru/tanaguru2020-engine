package com.tanaguru.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tanaguru.domain.JwtResponse;
import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.dto.AccessTokenDTO;
import com.tanaguru.domain.dto.AuthorizationCodeDTO;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.security.Oauth2JwtTokenUtil;
import com.tanaguru.service.OAuth2Service;
import com.tanaguru.service.TanaguruUserDetailsService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/authentication")
public class Oauth2AuthenticationController {

    @Value("${oauth2.auth.enabled}")
    private boolean oauth2AuthEnabled;

    @Value("${oauth2.oidc.enabled}")
    private boolean oauth2OidcEnabled;

    private final OAuth2Service oauth2Service;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final Oauth2JwtTokenUtil oauth2JwtTokenUtil;

    @Autowired
    public Oauth2AuthenticationController(TanaguruUserDetailsService tanaguruUserDetailsService,
            Oauth2JwtTokenUtil oauth2JwtTokenUtil, OAuth2Service oauth2Service) {
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.oauth2JwtTokenUtil = oauth2JwtTokenUtil;
        this.oauth2Service = oauth2Service;
    }

    /**
     * Check if the access token is valid and return a jwt authentication token
     * 
     * @param access_token
     * @return jwt authentication token
     */
    @ApiOperation(value = "Get a jwt token for a given access token", notes = "If access token incorrect, exception raise : ACCESS_DENIED", response = JwtResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message") })
    @PostMapping(value = "/oauth2/login", produces = { MediaType.APPLICATION_JSON_VALUE })
    public JwtResponse createOauth2AuthenticationToken(
            @RequestBody @ApiParam(required = true) AccessTokenDTO accessToken) {
        return this.oauth2Service.createJwtTokenFromAccessToken(accessToken);
    }

    /**
     * Refresh and return new jwt token
     * 
     * @return jwt authentication token
     */
    @ApiOperation(value = "Refresh and return new jwt token", notes = "If access token incorrect, exception raise : ACCESS_DENIED", response = JwtResponse.class)
    @ApiResponses(value = { @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message") })
    @PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
    @GetMapping(value = "/oauth2/refresh-token", produces = { MediaType.APPLICATION_JSON_VALUE })
    public JwtResponse refreshToken() {
        String token = "";
        try {
            UserDetails userDetails = tanaguruUserDetailsService
                    .loadUserByUsername(tanaguruUserDetailsService.getCurrentUser().getUsername());
            token = oauth2JwtTokenUtil.generateToken(userDetails);
        } catch (UsernameNotFoundException e) {
            throw new CustomForbiddenException(CustomError.ACCESS_DENIED);
        }
        return new JwtResponse(token);
    }

    @GetMapping(value = "/oauth2/auth-enabled", produces = { MediaType.APPLICATION_JSON_VALUE })
    public boolean isOauth2AuthEnabled() {
        return oauth2AuthEnabled;
    }

    @GetMapping(value = "/oauth2/oidc-enabled", produces = { MediaType.APPLICATION_JSON_VALUE })
    public boolean isOauth2OidcEnabled() {
        return oauth2OidcEnabled;
    }

    /**
     * Return an access token corresponding to the authorization code
     * 
     * @param authorization code
     * @return access token
     */
    @ApiOperation(value = "Get an access token for a given authorization code", notes = "If authorization code incorrect, exception raise : ACCESS_DENIED", response = AccessTokenDTO.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid parameters"),
            @ApiResponse(code = 401, message = "Unauthorized : ACCESS_DENIED message") })
    @PostMapping(value = "/oauth2/access-token", produces = { MediaType.APPLICATION_JSON_VALUE })
    public AccessTokenDTO getOauth2AccessToken(@RequestBody AuthorizationCodeDTO authorizationCode) {
        return this.oauth2Service.getAccessToken(authorizationCode.getCode());
    }
}
