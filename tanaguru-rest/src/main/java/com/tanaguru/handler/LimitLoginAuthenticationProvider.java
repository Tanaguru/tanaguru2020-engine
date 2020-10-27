package com.tanaguru.handler;

import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import com.tanaguru.domain.entity.membership.user.Attempt;
import com.tanaguru.service.UserService;

@Component
public class LimitLoginAuthenticationProvider extends DaoAuthenticationProvider {

    @Autowired
    UserService userService;

    @Autowired
    private HttpServletRequest request;


    @Override
    public Authentication authenticate(Authentication authentication)throws AuthenticationException {
        String ipAddress = request.getRemoteAddr();
        try {
            Authentication auth = super.authenticate(authentication);
            userService.resetFailAttempts(authentication.getName()); //sucess login, reset user attempts
            return auth;		

        } catch (BadCredentialsException e) {
            //invalid login, update user attempts
            userService.updateFailAttempts(authentication.getName(),ipAddress);
            throw e;

        } catch (LockedException e){
            //user locked
            String error = "";
            ArrayList<Attempt> attempts = userService.getAttempts(authentication.getName());
            if(!attempts.isEmpty()){
                Date lastAttempts = attempts.get(attempts.size()-1).getLastModified();
                Date blockedUntil = attempts.get(attempts.size()-1).getBlockedUntil();
                error = "User account is locked - Username : "+ authentication.getName() + " - Last attempts : " + lastAttempts + " - Blocked until : "+blockedUntil;
            }else{
                error = e.getMessage();
            }
            System.out.println(error);
            throw new LockedException(error);
        }
    }

    @Autowired
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }

}