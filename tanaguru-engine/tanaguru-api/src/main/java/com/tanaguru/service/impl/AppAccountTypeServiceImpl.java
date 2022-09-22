package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppAccountType;
import com.tanaguru.domain.entity.membership.user.AppAccountType;
import com.tanaguru.domain.exception.CustomIllegalStateException;
import com.tanaguru.repository.AppAccountTypeRepository;
import com.tanaguru.service.AppAccountTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AppAccountTypeServiceImpl implements AppAccountTypeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppAccountTypeServiceImpl.class);
    private Map<EAppAccountType, AppAccountType> appAccountTypeMap = new EnumMap<>(EAppAccountType.class);

    private final AppAccountTypeRepository appAccountTypeRepository;

    public AppAccountTypeServiceImpl(AppAccountTypeRepository appAccountTypeRepository) {
        this.appAccountTypeRepository = appAccountTypeRepository;
    }

    @PostConstruct
    public void initRoleMap() {
        LOGGER.debug("Initialize app account type map");
        for (EAppAccountType accountType : EAppAccountType.values()) {
            AppAccountType appAccountType = appAccountTypeRepository.findByName(accountType)
                    .orElseThrow(() -> new CustomIllegalStateException(CustomError.APP_ACCOUNT_TYPE_NOT_FOUND, accountType.toString() ));
            appAccountTypeMap.put(accountType, appAccountType);

        }
    }

    public Optional<AppAccountType> getAppAccountType(EAppAccountType appAccountType) {
        return Optional.ofNullable(appAccountTypeMap.get(appAccountType));
    }

}

