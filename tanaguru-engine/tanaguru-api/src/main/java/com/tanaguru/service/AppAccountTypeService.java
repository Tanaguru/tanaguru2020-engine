package com.tanaguru.service;

import java.util.Optional;

import com.tanaguru.domain.constant.EAppAccountType;
import com.tanaguru.domain.entity.membership.user.AppAccountType;

/**
 * @author lpedrau
 */
public interface AppAccountTypeService {
    /**
     * Find an @see AppAccountType in the preinitialized map
     *
     * @param appAccountType the @see EAppAccountType to resolve
     * @return The resolved @see AppAccountType
     */
    Optional<AppAccountType> getAppAccountType(EAppAccountType appAccountType);
}
