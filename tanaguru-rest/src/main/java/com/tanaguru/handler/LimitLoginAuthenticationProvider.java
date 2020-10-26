package com.tanaguru.handler;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tanaguru.service.TanaguruUserDetailsService;
import com.tanaguru.service.UserService;
import com.tanaguru.domain.entity.membership.user.UserAttempts;

@Component
public class LimitLoginAuthenticationProvider extends DaoAuthenticationProvider {

	@Autowired
	UserService userService;


	@Override
	public Authentication authenticate(Authentication authentication)throws AuthenticationException {
		try {
			Authentication auth = super.authenticate(authentication);
			userService.resetFailAttempts(authentication.getName());
			return auth;
			
		} catch (BadCredentialsException e) {
			//invalid login, update to user_attempts
			userService.updateFailAttempts(authentication.getName());
			throw e;
			
		} catch (LockedException e){
			//this user is locked!
			String error = "";
			Optional<UserAttempts> userAttempts = userService.getUserAttempts(authentication.getName());
			if(!userAttempts.isEmpty()){
				Date lastAttempts = userAttempts.get().getLastModified();
				error = "User account is locked! <br><br>Username : "+ authentication.getName() + "<br>Last Attempts : " + lastAttempts;
			}else{
				error = e.getMessage();
			}
			throw new LockedException(error);
		}
	}
	
	@Autowired
	@Override
	public void setUserDetailsService(UserDetailsService userDetailsService) {
		super.setUserDetailsService(userDetailsService);
	}
	
	//@Override
    //protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    	//super.additionalAuthenticationChecks(userDetails, authentication);
    //}
	

}