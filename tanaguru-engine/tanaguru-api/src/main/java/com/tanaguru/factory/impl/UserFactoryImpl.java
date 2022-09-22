package com.tanaguru.factory.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppAccountType;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.factory.UserFactory;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AppAccountTypeService;
import com.tanaguru.service.AppRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UserFactoryImpl implements UserFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(UserFactoryImpl.class);

    private final UserRepository userRepository;
    private final AppRoleService appRoleService;
    private final AppAccountTypeService appAccountTypeService;

    @Autowired
    public UserFactoryImpl(UserRepository userRepository, AppRoleService appRoleService, AppAccountTypeService appAccountTypeService) {
        this.userRepository = userRepository;
        this.appRoleService = appRoleService;
        this.appAccountTypeService = appAccountTypeService;
    }

    public User createUser(String username, String email, String password, EAppRole role, boolean isEnabled, String firstName, String lastName, EAppAccountType accountType) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setEnabled(isEnabled);
        user.setDateCreation(new Date());
        user.setAppRole(appRoleService.getAppRole(role)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.APP_ROLE_NOT_FOUND, role.name() )));
        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setAppAccountType(appAccountTypeService.getAppAccountType(accountType)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.APP_ACCOUNT_TYPE_NOT_FOUND, accountType.name())));
        user = userRepository.save(user);
        LOGGER.info("[User {}] Create with username {} and role {}", user.getId(), username, role);

        return user;
    }
}
