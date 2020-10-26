package com.tanaguru.handler;

import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
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
			userService.resetFailAttempts(authentication.getName()); //sucess login, reset user attempts
			return auth;
			
		} catch (BadCredentialsException e) {
			//invalid login, update user_attempts
			userService.updateFailAttempts(authentication.getName());
			throw e;
			
		} catch (LockedException e){
			//user locked
			String error = "";
			Optional<UserAttempts> userAttempts = userService.getUserAttempts(authentication.getName());
			if(!userAttempts.isEmpty()){
				Date lastAttempts = userAttempts.get().getLastModified();
				error = "User account is locked - Username : "+ authentication.getName() + " - Last attempts : " + lastAttempts;
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

}