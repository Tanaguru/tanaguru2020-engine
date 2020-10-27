package com.tanaguru.service.impl;

import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.user.Attempt;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.ContractUserRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AppRoleService;
import com.tanaguru.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
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

    private static final int FIRST_STEP_ATTEMPTS = 3;
    private static final int SECOND_STEP_ATTEMPTS = 5;
    private static final int MAX_ATTEMPTS = 7;
    private static final int FIRST_ATTEMPT_TIME = 300000; //5min
    private static final int SECOND_ATTEMPT_TIME = 43200000;  //12h

    public UserServiceImpl(UserRepository userRepository, 
            ContractRepository contractRepository, 
            ContractUserRepository contractUserRepository, 
            AppRoleService appRoleService) {
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.contractUserRepository = contractUserRepository;
        this.appRoleService = appRoleService;
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
     * 
     * @param username
     * @param ip
     */
    public void updateFailAttempts(String username, String ip) {
        ArrayList<Attempt> attempts = getAttempts(username);
        if (attempts.isEmpty()) {
            if (isUserExists(username)) {
                Attempt attempt = new Attempt();
                Optional<User> user = userRepository.findByUsername(username);
                attempt.setNumber(1);
                attempt.setIp(ip);
                attempt.setLastModified(new Date());
                attempts.add(attempt);
                user.get().setAttempts(attempts);
                userRepository.save(user.get());
            }
        } else {
            if (isUserExists(username)) {
                //update attempts +1
                Optional<User> user = userRepository.findByUsername(username);
                Attempt lastAttempt = attempts.get(attempts.size()-1);
                Attempt actualAttempt = new Attempt();
                actualAttempt.setNumber(lastAttempt.getNumber()+1);
                actualAttempt.setIp(ip);
                actualAttempt.setLastModified(new Date());
                attempts.add(actualAttempt);
                user.get().setAttempts(attempts);
                userRepository.save(user.get());
            }
            Optional<User> user = userRepository.findByUsername(username);
            switch(attempts.get(attempts.size()-1).getNumber()) {
            case FIRST_STEP_ATTEMPTS:
                //locked user
                blockAccount(user, attempts,FIRST_ATTEMPT_TIME);
                new java.util.Timer().schedule( 
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                user.get().setAccountNonLocked(true);
                                userRepository.save(user.get());
                            }
                        }, 
                        FIRST_ATTEMPT_TIME
                        );
                break;
            case SECOND_STEP_ATTEMPTS:
                //locked user
                blockAccount(user, attempts,SECOND_ATTEMPT_TIME);
                new java.util.Timer().schedule( 
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                user.get().setAccountNonLocked(true);
                                userRepository.save(user.get());
                            }
                        }, 
                        SECOND_ATTEMPT_TIME
                        );
                break;
            case MAX_ATTEMPTS:
                //locked user definitely
                if(!user.isEmpty()) {
                    user.get().setAccountNonLocked(false);
                    userRepository.save(user.get());
                }
                //envoyer un mail au super admin avec la liste des tentatives 
                break;
       
            default:
            }
        }
    }

    /**
     * 
     * @param user
     * @param attempts
     * @param duration
     */
    private void blockAccount(Optional<User> user, ArrayList<Attempt> attempts, int duration) {
        if(!user.isEmpty()) {
            user.get().setAccountNonLocked(false);
            attempts.get(attempts.size()-1).setBlockedUntil(actualDateAdd(duration));
            userRepository.save(user.get());
        }
    }
    
    /**
     * 
     * @param username
     */
    public void resetFailAttempts(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(!user.isEmpty()) {
            user.get().setAttempts(new ArrayList<Attempt>());
        }
    }

    /**
     * 
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
     * 
     * @param username
     * @return
     */
    private boolean isUserExists(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return !user.isEmpty();
    }

    /**
     * 
     * @param miliseconds
     * @return
     */
    private Date actualDateAdd(int miliseconds) {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss");
        calendar.add(Calendar.MILLISECOND, miliseconds);
        Date addMilliSeconds = calendar.getTime();
        return addMilliSeconds;
    }
}
