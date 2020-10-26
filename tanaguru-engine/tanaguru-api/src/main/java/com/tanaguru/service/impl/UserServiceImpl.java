package com.tanaguru.service.impl;

import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.entity.membership.user.UserAttempts;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.ContractUserRepository;
import com.tanaguru.repository.UserAttemptsRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AppRoleService;
import com.tanaguru.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

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
    private final UserAttemptsRepository userAttemptsRepository;
    
    private static final int MAX_ATTEMPTS = 3;

    public UserServiceImpl(UserRepository userRepository, 
    		ContractRepository contractRepository, 
    		ContractUserRepository contractUserRepository, 
    		AppRoleService appRoleService,
    		UserAttemptsRepository userAttemptsRepository) {
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.contractUserRepository = contractUserRepository;
        this.appRoleService = appRoleService;
        this.userAttemptsRepository = userAttemptsRepository;
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
    
    public void updateFailAttempts(String username) {
    	Optional<UserAttempts> userAttempts = getUserAttempts(username);
    	if (userAttempts.isEmpty()) {
    		if (isUserExists(username)) {
    			UserAttempts userAttemptsInfo = new UserAttempts();
    			userAttemptsInfo.setAttempts(1);
    			userAttemptsInfo.setUsername(username);
    			userAttemptsInfo.setLastModified(new Date());
    			userAttemptsRepository.save(userAttemptsInfo);
    		}
    	} else {
    		if (isUserExists(username)) {
    			//update attempts +1
    			int attempts = userAttempts.get().getAttempts()+1;
    			userAttempts.get().setAttempts(attempts);
    			userAttempts.get().setLastModified(new Date());
    			userAttemptsRepository.save(userAttempts.get());
    		}
    		if (userAttempts.get().getAttempts() >= MAX_ATTEMPTS) {
    			//locked user
    			Optional<User> user = userRepository.findByUsername(username);
    			if(!user.isEmpty()) {
    				user.get().setAccountNonLocked(false);
    				userRepository.save(user.get());
    			}
    			//throw exception
    			//throw new LockedException("User Account is locked!");
    		}
    	}
    }
    
    public void resetFailAttempts(String username) {
    	 Optional<UserAttempts> userAttempts = userAttemptsRepository.findByUsername(username);
    	 if(!userAttempts.isEmpty()) {
    		 userAttempts.get().setAttempts(0);
    		 userAttempts.get().setLastModified(null);
    	 }
    }
    
    public Optional<UserAttempts> getUserAttempts(String username) {
    	Optional<UserAttempts> userAttempts = userAttemptsRepository.findByUsername(username);
    	return userAttempts;
    }
    
    private boolean isUserExists(String username) {
  	  Optional<User> user = userRepository.findByUsername(username);
  	  return !user.isEmpty();
  	}
}
