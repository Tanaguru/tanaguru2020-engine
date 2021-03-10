package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.user.Attempt;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.ContractUserRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AppRoleService;
import com.tanaguru.service.ContractService;
import com.tanaguru.service.MailService;
import com.tanaguru.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.transaction.Transactional;


/**
 * @author rcharre
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ContractUserRepository contractUserRepository;
    private final AppRoleService appRoleService;
    private final ContractService contractService;
    private final MailService mailService;

    private static final int FIRST_STEP_ATTEMPTS = 3;
    private static final int SECOND_STEP_ATTEMPTS = 5;
    private static final int MAX_ATTEMPTS = 7;
    private static final int FIRST_ATTEMPT_TIME = 300000; //5min
    private static final int SECOND_ATTEMPT_TIME = 43200000;  //12h
    private static final String ADMIN_MAIL = "support@tanaguru.com";
    private static final String ATTEMPTS_MAIL_SUBJECT_ADMIN = "Blocage d'un compte utilisateur";
    private static final String ATTEMPTS_MAIL_SUBJECT_USER = "Blocage de votre compte utilisateur";

    public UserServiceImpl(UserRepository userRepository,
                           ContractRepository contractRepository,
                           ContractUserRepository contractUserRepository,
                           AppRoleService appRoleService,
                           ContractService contractService, MailService mailService) {
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.contractUserRepository = contractUserRepository;
        this.appRoleService = appRoleService;
        this.contractService = contractService;
        this.mailService = mailService;
    }

    public boolean checkUsernameIsUsed(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean checkEmailIsUsed(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User modifyUser(User from, User to) {
        if (!from.getUsername().equals(to.getUsername()) &&  checkUsernameIsUsed(to.getUsername())) {
            throw new CustomInvalidEntityException(CustomError.USERNAME_ALREADY_EXISTS);
        } else {
            LOGGER.info("[User {}] set username to {}", from.getId(), to.getUsername());
            from.setUsername(to.getUsername());
        }

        if (!from.getEmail().equals(to.getEmail()) && checkEmailIsUsed(to.getEmail())) {
            throw new CustomInvalidEntityException(CustomError.EMAIL_ALREADY_EXISTS);
        } else {
            LOGGER.info("[User {}] set email to {}", from.getId(), to.getEmail());
            from.setEmail(to.getEmail());
        }

        from.setEnabled(to.isEnabled());

        if(!from.isAccountNonLocked() && to.isAccountNonLocked()) {
            from.setAttempts(new ArrayList<Attempt>());
        }
        from.setAccountNonLocked(to.isAccountNonLocked());
   
        if(to.getAppRole() != null){
            from.setAppRole(to.getAppRole());
        }

        return userRepository.save(from);
    }

    public void deleteUser(User user) {
        for (ContractAppUser contractUser : contractUserRepository.findAllByUserAndContractRole_Name_Owner(user)) {
            contractService.deleteContract(contractUser.getContract());
        }

        userRepository.delete(user);
        LOGGER.info("[User {}] deleted", user.getId());
    }

    public boolean hasAuthority(User user, String authority){
        return appRoleService.getAppAuthorityByAppRole(user.getAppRole().getName()).contains(authority);
    }

    /**
     * Update the fails attempts of the user
     * @param user
     * @param ip
     */
    public void updateFailAttempts(User user, String ip, boolean sendAdminMail) {
        ArrayList<Attempt> attempts = new ArrayList<Attempt>();
        attempts = new ArrayList<Attempt>(user.getAttempts());
        if (attempts.isEmpty()) {
            Attempt attempt = new Attempt();
            attempt.setNumber(1);
            attempt.setIp(ip);
            attempt.setLastModified(new Date());
            attempts.add(attempt);
            user.setAttempts(attempts);
            userRepository.save(user);     
        } else {
            //update attempts +1
            Attempt lastAttempt = attempts.get(attempts.size()-1);
            Attempt currentAttempt = new Attempt();
            currentAttempt.setNumber(lastAttempt.getNumber()+1);
            currentAttempt.setIp(ip);
            currentAttempt.setLastModified(new Date());
            attempts.add(currentAttempt);
            user.setAttempts(attempts);
            userRepository.save(user);

            switch(attempts.get(attempts.size()-1).getNumber()) {

            case FIRST_STEP_ATTEMPTS: 
                blockAccount(user, attempts,FIRST_ATTEMPT_TIME);
                break;

            case SECOND_STEP_ATTEMPTS:
                blockAccount(user, attempts,SECOND_ATTEMPT_TIME);
                break;

            case MAX_ATTEMPTS:
                //block user definitely
                blockAccount(user, attempts,0);
                //send mail to super admin with list of attempts
                DateFormat longDateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
                StringBuilder builder = new StringBuilder();
                builder.append("Multiples tentatives de connexions sur le compte : ")
                        .append(user.getEmail()).append("\nNom d'utilisateur : ")
                        .append(user.getUsername()).append("\nID utilisateur : ")
                        .append(user.getId());
                for(Attempt attempt : attempts) {
                    builder.append("\n\nTentative numéro ")
                            .append(attempt.getNumber()).append(" | IP : ")
                            .append(attempt.getIp()).append(" | Dernier accés : ")
                            .append(longDateFormat.format(attempt.getLastModified()));
                    if( attempt.getBlockedUntil() != null ){
                        builder.append(" | Bloqué jusqu'à : ")
                                .append(longDateFormat.format(attempt.getBlockedUntil()));
                    }
                }
                try {
                    if(sendAdminMail) {
                        mailService.sendSimpleMessage(ADMIN_MAIL,ATTEMPTS_MAIL_SUBJECT_ADMIN, builder.toString());
                        LOGGER.info("[User {}] account blocking email sent to admin", user.getId());
                    }
                }catch(MailException e) {
                    LOGGER.error("[User {}] Unable to send the account blocking email to admin", user.getId());
                }
                try {
                    mailService.sendSimpleMessage(user.getEmail(), ATTEMPTS_MAIL_SUBJECT_USER, builder.toString());
                    LOGGER.info("[User {}] account blocking email sent to user", user.getId());
                }catch(MailException e) {
                    LOGGER.error("[User {}] Unable to send the account blocking email to user", user.getId());
                }
                
                break;

            default: //Do nothing
            }
        }
    }


    /**
     * Block the account of the user during duration time set
     * @param user
     * @param attempts attempts of the user
     * @param duration blocking time
     */
    private void blockAccount(User user, ArrayList<Attempt> attempts, int duration) {
        user.setAccountNonLocked(false);
        if(duration != 0) {
            attempts.get(attempts.size()-1).setBlockedUntil(currentDateAdd(duration));
        }
        userRepository.save(user);
    }

    /**
     * Reset the attempts of the user
     * @param user
     */
    public void resetFailAttempts(User user) {
        user.setAttempts(new ArrayList<Attempt>());
        userRepository.save(user);    
    }

    /**
     * Set accountNonLocked to true for the user
     * @param user
     */
    public void unlock(User user) {
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    /**
     * Return the current date plus milliseconds 
     * @param miliseconds
     * @return
     */
    private Date currentDateAdd(int miliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, miliseconds);
        Date addMilliSeconds = calendar.getTime();
        return addMilliSeconds;
    }
}
