package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppAccountType;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.user.ApiKey;
import com.tanaguru.domain.entity.membership.user.Attempt;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.entity.membership.user.UserToken;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.factory.UserFactory;
import com.tanaguru.repository.ContractUserRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.repository.UserTokenRepository;
import com.tanaguru.service.AppRoleService;
import com.tanaguru.service.ContractService;
import com.tanaguru.service.MailService;
import com.tanaguru.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;


/**
 * @author rcharre
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ContractUserRepository contractUserRepository;
    private final AppRoleService appRoleService;
    private final ContractService contractService;
    private final MailService mailService;
    private final MessageService messageService;
    private final UserFactory userFactory;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final UserTokenRepository userTokenRepository;

    private static final int FIRST_STEP_ATTEMPTS = 3;
    private static final int SECOND_STEP_ATTEMPTS = 5;
    private static final int MAX_ATTEMPTS = 7;
    private static final int FIRST_ATTEMPT_TIME = 300000; //5min
    private static final int SECOND_ATTEMPT_TIME = 43200000;  //12h
    private static final String ADMIN_MAIL = "support@tanaguru.com";

    public UserServiceImpl(UserRepository userRepository,
                           ContractUserRepository contractUserRepository,
                           AppRoleService appRoleService,
                           ContractService contractService, MailService mailService,
                           MessageService messageService, UserFactory userFactory,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           UserTokenRepository userTokenRepository) {
        this.userRepository = userRepository;
        this.contractUserRepository = contractUserRepository;
        this.appRoleService = appRoleService;
        this.contractService = contractService;
        this.mailService = mailService;
        this.messageService = messageService;
        this.userFactory = userFactory;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userTokenRepository = userTokenRepository;
    }

    public boolean checkUsernameIsUsed(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean checkEmailIsUsed(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User createUser(
            String username, 
            String email, 
            String password, 
            EAppRole appRole, 
            boolean enabled, 
            boolean createContract,
            String firstName, 
            String lastName,
            EAppAccountType appAccountType) {
        if (checkUsernameIsUsed(username)) {
            throw new CustomInvalidEntityException(CustomError.USERNAME_ALREADY_EXISTS);
        }

        if (checkEmailIsUsed(email)) {
            throw new CustomInvalidEntityException(CustomError.EMAIL_ALREADY_EXISTS);
        }

        if (password == null || password.isEmpty()) {
            throw new CustomInvalidEntityException(CustomError.INVALID_PASSWORD);
        }


        User user = userFactory.createUser(username, email, bCryptPasswordEncoder.encode(password), appRole, enabled, firstName, lastName, appAccountType);
        if (createContract) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 1);
            contractService.createContract(user, user.getUsername(), 0, 0, true, calendar.getTime(), true, true);
        }
        return user;
    }


    public User modifyUser(User from, User to) {
        if (!from.getUsername().equals(to.getUsername()) && checkUsernameIsUsed(to.getUsername())) {
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

        if (!from.isAccountNonLocked() && to.isAccountNonLocked()) {
            from.setAttempts(new ArrayList<Attempt>());
        }
        from.setAccountNonLocked(to.isAccountNonLocked());

        if (to.getAppRole() != null) {
            from.setAppRole(to.getAppRole());
        }
        
        if ( to.getFirstname() != null && from.getFirstname() != null && !from.getFirstname().equals(to.getFirstname())) {
            LOGGER.info("[User {}] set firstname to {}", from.getId(), to.getFirstname());
            from.setFirstname(to.getFirstname());
        }
        
        if ( to.getLastname() != null && from.getLastname() != null && !from.getLastname().equals(to.getLastname())) {
            LOGGER.info("[User {}] set lastname to {}", from.getId(), to.getLastname());
            from.setLastname(to.getLastname());
        }
        
        return userRepository.save(from);
    }

    public void deleteUser(User user) {
        contractUserRepository.findAllByUserAndContractRole_Name_Owner(user)
                .stream()
                .map(ContractAppUser::getContract)
                .forEach(contractService::deleteContract);

        contractUserRepository.deleteAllByUser(user);
        userRepository.delete(user);
        LOGGER.info("[User {}] deleted", user.getId());
    }

    public boolean hasAuthority(User user, String authority) {
        return appRoleService.getAppAuthorityByAppRole(user.getAppRole().getName()).contains(authority);
    }

    /**
     * Update the fails attempts of the user
     *
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
            Attempt lastAttempt = attempts.get(attempts.size() - 1);
            Attempt currentAttempt = new Attempt();
            currentAttempt.setNumber(lastAttempt.getNumber() + 1);
            currentAttempt.setIp(ip);
            currentAttempt.setLastModified(new Date());
            attempts.add(currentAttempt);
            user.setAttempts(attempts);
            userRepository.save(user);

            switch (attempts.get(attempts.size() - 1).getNumber()) {

                case FIRST_STEP_ATTEMPTS:
                    blockAccount(user, attempts, FIRST_ATTEMPT_TIME);
                    break;

                case SECOND_STEP_ATTEMPTS:
                    blockAccount(user, attempts, SECOND_ATTEMPT_TIME);
                    break;

                case MAX_ATTEMPTS:
                    //block user definitely
                    blockAccount(user, attempts, 0);
                    //send mail to super admin with list of attempts
                    DateFormat longDateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
                    StringBuilder builder = new StringBuilder();
                    builder.append(messageService.getMessage("mail.block.user.attempts.email"))
                            .append(user.getEmail()).append("\n" + messageService.getMessage("mail.block.user.attempts.username"))
                            .append(user.getUsername()).append("\n" + messageService.getMessage("mail.block.user.attempts.userid"))
                            .append(user.getId());
                    for (Attempt attempt : attempts) {
                        builder.append("\n\n" + messageService.getMessage("mail.block.user.attempts.number"))
                                .append(attempt.getNumber()).append(" | IP : ")
                                .append(attempt.getIp()).append(" | " + messageService.getMessage("mail.block.user.attempts.lastAttempt"))
                                .append(longDateFormat.format(attempt.getLastModified()));
                        if (attempt.getBlockedUntil() != null) {
                            builder.append(" | " + messageService.getMessage("mail.block.user.attempts.until"))
                                    .append(longDateFormat.format(attempt.getBlockedUntil()));
                        }
                    }
                    try {
                        if (sendAdminMail) {
                            mailService.sendSimpleMessage(ADMIN_MAIL, messageService.getMessage("mail.block.user.attempts.adminSubject"), builder.toString());
                            LOGGER.info("[User {}] account blocking email sent to admin", user.getId());
                        }
                    } catch (MailException e) {
                        LOGGER.error("[User {}] Unable to send the account blocking email to admin", user.getId());
                    }
                    try {
                        mailService.sendSimpleMessage(user.getEmail(), messageService.getMessage("mail.block.user.attempts.subject"), builder.toString());
                        LOGGER.info("[User {}] account blocking email sent to user", user.getId());
                    } catch (MailException e) {
                        LOGGER.error("[User {}] Unable to send the account blocking email to user", user.getId());
                    }

                    break;

                default: //Do nothing
            }
        }
    }


    /**
     * Block the account of the user during duration time set
     *
     * @param user
     * @param attempts attempts of the user
     * @param duration blocking time
     */
    private void blockAccount(User user, ArrayList<Attempt> attempts, int duration) {
        LOGGER.info("[User {}] block account with {} attempts for {} ", user.getId(), attempts.size(), duration);
        user.setAccountNonLocked(false);
        if (duration != 0) {
            attempts.get(attempts.size() - 1).setBlockedUntil(currentDateAdd(duration));
        }
        userRepository.save(user);
    }

    /**
     * Reset the attempts of the user
     *
     * @param user
     */
    public void resetFailAttempts(User user) {
        LOGGER.info("[User {}] reset authentication attempts ", user.getId());
        user.setAttempts(new ArrayList<Attempt>());
        userRepository.save(user);
    }

    /**
     * Set accountNonLocked to true for the user
     *
     * @param user
     */
    public void unlock(User user) {
        LOGGER.info("[User {}] unlock ", user.getId());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    /**
     * Return the current date plus milliseconds
     *
     * @param miliseconds
     * @return
     */
    private Date currentDateAdd(int miliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, miliseconds);
        return calendar.getTime();
    }

    /**
     * Generate a new Api key. The key is store with the user.
     * This key can authenticate the user.
     * @param user
     * @return user token
     */
    @Override
    public String generateUserToken(User user, Date expiration) {
        String userTokenValue = "";
        if(user != null) {
            userTokenValue = RandomStringUtils.random(20, true, true);
            Optional<UserToken> oldUserToken = this.userTokenRepository.findByUser(user);
            if(oldUserToken.isPresent()) {
                oldUserToken.get().setToken(userTokenValue);
                oldUserToken.get().setExpiration(expiration);
                this.userTokenRepository.save(oldUserToken.get());
            }else {
                UserToken userToken = new UserToken(user, userTokenValue, expiration);
                this.userTokenRepository.save(userToken);
            }
        }else {
            throw new CustomEntityNotFoundException(CustomError.CANNOT_GENERATE_API_KEY);
        }
        return userTokenValue;
    }
}
