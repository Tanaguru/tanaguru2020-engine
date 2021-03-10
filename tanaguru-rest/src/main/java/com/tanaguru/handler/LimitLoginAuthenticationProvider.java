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

    private final UserService userService;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Autowired
    public LimitLoginAuthenticationProvider(UserService userService, UserRepository userRepository, HttpServletRequest request) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.request = request;
    }

    @Override
    public Authentication authenticate(Authentication authentication)throws AuthenticationException {
        String ipAddress = request.getRemoteAddr();
        Optional<User> user = userRepository.findByUsername(authentication.getName());
        try {
            Authentication auth = super.authenticate(authentication);
            //sucess login, reset user attempts
            user.ifPresent(value -> userService.resetFailAttempts(value));
            return auth;		

        } catch (BadCredentialsException e) {
            //invalid login, update user attempts
            user.ifPresent(value -> userService.updateFailAttempts(value, ipAddress, sendAdminMail));
            throw e;

        } catch (LockedException e){
            //user locked       
            String error = "";
            Date now = new Date();
            ArrayList<Attempt> attempts = new ArrayList<Attempt>();
            if(user.isPresent()) {
                attempts = new ArrayList<Attempt>(user.get().getAttempts());
            }
            if(!attempts.isEmpty()) {
                Date blockedUntil = attempts.get(attempts.size()-1).getBlockedUntil();
                if(blockedUntil != null){
                    if(now.after(blockedUntil)) { //verification time if the user can be unlock and authentication test again
                        userService.unlock(user.get());
                        return authenticate(authentication);
                    }else {
                        Date lastAttempts = attempts.get(attempts.size()-1).getLastModified();
                        error = "User account is locked - Username : "+ authentication.getName() + " - Last attempts : " + lastAttempts + " - Blocked until : "+blockedUntil;
                        LOGGER.info(error);
                        throw new LockedException(error);
                    }
                }else {
                    error = "User account is locked - Username : "+ authentication.getName();
                    LOGGER.info(error);
                    throw new LockedException(error);
                }
            }else {
                error = "User account is locked - Username : "+ authentication.getName();
                LOGGER.info(error);
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