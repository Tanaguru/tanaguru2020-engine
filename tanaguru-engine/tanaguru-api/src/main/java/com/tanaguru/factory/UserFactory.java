package com.tanaguru.factory;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.User;

public interface UserFactory {
    /**
     * @param username  The username
     * @param email     The user email address
     * @param password  The User encrypted password
     * @param role      The @See EAppRole
     * @param isEnabled True if the @see User is enabled
     * @return The new @see User
     */
    User createUser(String username, String email, String password, EAppRole role, boolean isEnabled, String firstName, String lastName);
}
