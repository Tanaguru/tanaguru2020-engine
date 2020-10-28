package com.tanaguru.service.impl;

import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.user.Attempt;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.ContractUserRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AppRoleService;
import com.tanaguru.service.MailService;
import com.tanaguru.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
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
            MailService mailService) {
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.contractUserRepository = contractUserRepository;
        this.appRoleService = appRoleService;
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
            throw new InvalidEntityException("Username already exists");
        } else {
            LOGGER.info("[User {}] set username to {}", from.getId(), to.getUsername());
            from.setUsername(to.getUsername());
        }

        if (!from.getEmail().equals(to.getEmail()) && checkEmailIsUsed(to.getEmail())) {
            throw new InvalidEntityException("Email already exists");
        } else {
            LOGGER.info("[User {}] set email to {}", from.getId(), to.getEmail());
            from.setEmail(to.getEmail());
        }

        from.setEnabled(to.isEnabled());

        if(to.getAppRole() != null){
            from.setAppRole(to.getAppRole());
        }

        return userRepository.save(from);
    }

    public void deleteUser(User user) {
        for (ContractAppUser contractUser : contractUserRepository.findAllByUser(user)) {
            contractRepository.delete(contractUser.getContract());
        }

        userRepository.delete(user);
        LOGGER.info("[User {}] deleted", user.getId());
    }

    public boolean hasAuthority(User user, String authority){
        return appRoleService.getAppAuthorityByAppRole(user.getAppRole().getName()).contains(authority);
    }

    /**
     * Update the fails attempts of the user
     * @param username
     * @param ip
     */
    public void updateFailAttempts(String username, String ip, boolean sendAdminMail) {
        ArrayList<Attempt> attempts = getAttempts(username);
        Optional<User> user = userRepository.findByUsername(username);
        if (attempts.isEmpty()) {
            if (!user.isEmpty()) {
                Attempt attempt = new Attempt();
                attempt.setNumber(1);
                attempt.setIp(ip);
                attempt.setLastModified(new Date());
                attempts.add(attempt);
                user.get().setAttempts(attempts);
                userRepository.save(user.get());
            }
        } else {
            if (!user.isEmpty()) {
                //update attempts +1
                Attempt lastAttempt = attempts.get(attempts.size()-1);
                Attempt currentAttempt = new Attempt();
                currentAttempt.setNumber(lastAttempt.getNumber()+1);
                currentAttempt.setIp(ip);
                currentAttempt.setLastModified(new Date());
                attempts.add(currentAttempt);
                user.get().setAttempts(attempts);
                userRepository.save(user.get());
            }
            switch(attempts.get(attempts.size()-1).getNumber()) {

            case FIRST_STEP_ATTEMPTS: 
                blockAccount(user, attempts,FIRST_ATTEMPT_TIME);
                break;

            case SECOND_STEP_ATTEMPTS:
                blockAccount(user, attempts,SECOND_ATTEMPT_TIME);
                break;

            case MAX_ATTEMPTS:
                //locked user definitely
                blockAccount(user, attempts,0);
                //send mail to super admin with list of attempts
                DateFormat longDateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
                StringBuilder builder = new StringBuilder();
                builder.append("Multiples tentatives de connexions sur le compte : "+user.get().getEmail());
                builder.append("\nNom d'utilisateur : "+user.get().getUsername());
                builder.append("\nID utilisateur : "+user.get().getId());
                for(Attempt attempt : attempts) {
                    builder.append("\n\nTentative numéro "+attempt.getNumber());
                    builder.append(" | IP : "+attempt.getIp());
                    builder.append(" | Dernier accès :"+longDateFormat.format(attempt.getLastModified()));
                    if( attempt.getBlockedUntil() != null ){
                        builder.append(" | Bloqué jusqu'à : "+longDateFormat.format(attempt.getBlockedUntil()));
                    }
                }
                if(sendAdminMail) {
                    mailService.sendSimpleMessage(ADMIN_MAIL,ATTEMPTS_MAIL_SUBJECT_ADMIN, builder.toString());
                }
                mailService.sendSimpleMessage(user.get().getEmail(), ATTEMPTS_MAIL_SUBJECT_USER, builder.toString());
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
    private void blockAccount(Optional<User> user, ArrayList<Attempt> attempts, int duration) {
        if(!user.isEmpty()) {
            user.get().setAccountNonLocked(false);
            if(duration != 0) {
                attempts.get(attempts.size()-1).setBlockedUntil(currentDateAdd(duration));
            }
            userRepository.save(user.get());
        }
    }

    /**
     * Reset the attempts of the user
     * @param username
     */
    public void resetFailAttempts(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(!user.isEmpty()) {
            user.get().setAttempts(new ArrayList<Attempt>());
        }
    }

    /**
     * Return the attempts of the user
     * @param username
     */
    public ArrayList<Attempt> getAttempts(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        ArrayList<Attempt> attempts = new ArrayList<Attempt>();
        if(!user.isEmpty()) {
            attempts = (ArrayList<Attempt>) user.get().getAttempts();
        }
        return attempts;
    }

    /**
     * Set accountNonLocked to true for the user
     * @param username
     */
    public void unlock(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(!user.isEmpty()) {
            user.get().setAccountNonLocked(true);;
        }
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
