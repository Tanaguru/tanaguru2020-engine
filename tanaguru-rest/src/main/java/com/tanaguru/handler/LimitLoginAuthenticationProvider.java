package com.tanaguru.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import com.tanaguru.domain.entity.membership.user.Attempt;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.UserService;

@Component
public class LimitLoginAuthenticationProvider extends DaoAuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitLoginAuthenticationProvider.class);  

    @Value("${admin.mail.whenblocked}")
    private boolean sendAdminMail;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

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
            userService.updateFailAttempts(authentication.getName(),ipAddress, sendAdminMail);
            throw e;

        } catch (LockedException e){
            //user locked       
            String error = "";
            Date now = new Date();
            Optional<User> user = userRepository.findByUsername(authentication.getName());
            ArrayList<Attempt> attempts = new ArrayList<Attempt>();
            if(!user.isEmpty()) {
                attempts = new ArrayList<Attempt>(user.get().getAttempts());
            }
            if(!attempts.isEmpty()) {
                Date blockedUntil = attempts.get(attempts.size()-1).getBlockedUntil();
                if(blockedUntil != null){
                    if(now.after(blockedUntil)) { //verification time if the user can be unlock and authentication test again
                        userService.unlock(authentication.getName());
                        return authenticate(authentication);
                    }else {
                        Date lastAttempts = attempts.get(attempts.size()-1).getLastModified();
                        error = "User account is locked - Username : "+ authentication.getName() + " - Last attempts : " + lastAttempts + " - Blocked until : "+blockedUntil;
                        LOGGER.info(error);
                        throw new LockedException(error);
                    }
                }else {
                    error = "User account is locked - Username : "+ authentication.getName();
                    throw new LockedException(error);
                }
            }else {
                error = "User account is locked - Username : "+ authentication.getName();
                throw new LockedException(error);
            }
        }
    }

    @Autowired
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        super.setUserDetailsService(userDetailsService);
    }
}