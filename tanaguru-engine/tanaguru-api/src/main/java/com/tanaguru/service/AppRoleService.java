package com.tanaguru.service;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.AppRole;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
public interface AppRoleService {
    /**
     * Find an @see AppRole in the preinitialized map
     *
     * @param appRole the @see EAppRole to resolve
     * @return The resolved @see AppRole
     */
    Optional<AppRole> getAppRole(EAppRole appRole);


    /**
     * Find a list of @see AppAuthorityName for a given @see EAppRole
     * @param appRole The given @see EAppRole
     * @return The list of @see AppAuthorityName
     */
    Collection<String> getAppAuthorityByAppRole(EAppRole appRole);
}
