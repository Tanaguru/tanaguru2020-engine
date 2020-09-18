package com.tanaguru.service.impl;

import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
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

    public UserServiceImpl(UserRepository userRepository, ContractRepository contractRepository, ContractUserRepository contractUserRepository, AppRoleService appRoleService) {
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
}
