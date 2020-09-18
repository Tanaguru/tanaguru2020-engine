package com.tanaguru.service;

import com.tanaguru.domain.entity.membership.user.User;

public interface UserService {

    /**
     * Check id a user with same email already exists
     *
     * @param email the email address
     * @return True if the email address is already used
     */
    boolean checkEmailIsUsed(String email);

    /**
     * Check id a user with same username already exists
     *
     * @param username the username
     * @return True if the username is already used
     */
    boolean checkUsernameIsUsed(String username);

    /**
     * Modify a @see User
     *
     * @param from The @see User to change
     * @param to   The modifications
     * @return The saved @see User
     */
    User modifyUser(User from, User to);

    /**
     * Delete an @see User and all his contracts
     *
     * @param user The @see User to delete
     */
    void deleteUser(User user);

    /**
     * Check if an @see User has an auhority
     * @param user The @see User
     * @param authority The authority
     * @return True if the @see User has the authority
     */
    boolean hasAuthority(User user, String authority);
}
