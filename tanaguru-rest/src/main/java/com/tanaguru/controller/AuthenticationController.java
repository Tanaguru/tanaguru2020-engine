package com.tanaguru.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tanaguru.domain.JwtRequest;
import com.tanaguru.domain.JwtResponse;
import com.tanaguru.security.JwtTokenUtil;
import com.tanaguru.service.TanaguruUserDetailsService;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {
	private final TanaguruUserDetailsService tanaguruUserDetailsService;
	private final JwtTokenUtil jwtTokenUtil;
	private final AuthenticationManager authenticationManager;

	@Autowired
	public AuthenticationController(TanaguruUserDetailsService tanaguruUserDetailsService, JwtTokenUtil jwtTokenUtil,
			AuthenticationManager authenticationManager) {
		this.tanaguruUserDetailsService = tanaguruUserDetailsService;
		this.jwtTokenUtil = jwtTokenUtil;
		this.authenticationManager = authenticationManager;
	}

	@PostMapping(value = "/login", produces = { MediaType.APPLICATION_JSON_VALUE })
	public JwtResponse createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
				authenticationRequest.getPassword()));
		UserDetails userDetails = tanaguruUserDetailsService.loadUserByUsername(authenticationRequest.getUsername());

		String token = jwtTokenUtil.generateToken(userDetails);
		return new JwtResponse(token);
	}

	@PreAuthorize("@tanaguruUserDetailsServiceImpl.getCurrentUser() != null")
	@GetMapping(value = "/refresh-token", produces = { MediaType.APPLICATION_JSON_VALUE })
	public JwtResponse refreshToken() {
		UserDetails userDetails = tanaguruUserDetailsService
				.loadUserByUsername(tanaguruUserDetailsService.getCurrentUser().getUsername());
		String token = jwtTokenUtil.generateToken(userDetails);
		return new JwtResponse(token);
	}

	@ApiIgnore
	@GetMapping(value = "/logout")
	public void logout() {}

	/**
	 * Authenticate user by api key and return jwt token
	 *
	 * @param apiKey
	 * @return jwt token
	 */
	@PostMapping(value = "/api-key/{apiKey}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public JwtResponse createAuthentTokenForApiKey(@PathVariable String apiKey) {
		UserDetails userDetails = tanaguruUserDetailsService.loadUserByApiKey(apiKey);
		String token = jwtTokenUtil.generateToken(userDetails);
		return new JwtResponse(token);
	}

	/**
	 * Authenticate user by user token and return jwt token
	 *
	 * @param userToken
	 * @return jwt token
	 */
	@PostMapping(value = "/user-token/{userToken}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public JwtResponse createAuthenticationTokenByUserToken(@PathVariable String userToken) {
		UserDetails userDetails = tanaguruUserDetailsService.loadUserByUserToken(userToken);
		String token = jwtTokenUtil.generateToken(userDetails);
		return new JwtResponse(token);
	}
}
