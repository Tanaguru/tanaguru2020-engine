package com.tanaguru.service;

import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.user.User;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface TanaguruUserDetailsService extends UserDetailsService{

    /**
     * Get the current @see User
     *
     * @return The current @see User
     */
    User getCurrentUser();

    /**
     * Check if the current @see User has the given ContractAuthority
     * @param authority The @see ContractAuthorityName
     * @param contractId The id of the @see Contract
     * @return True if the @see User has the @see ContractAuthority
     */
    boolean currentUserHasAuthorityOnContract(String authority, long contractId);
    
    /**
     * Check if the current @see User has the given ProjectAuthority
     * @param authority The @see ProjectAuthorityName
     * @param projectId The id of the @see Project
     * @return True if the @see User has the @see ProjectAuthority
     */
    boolean currentUserHasAuthorityOnProject(String authority, long projectId);

    /**
     * Check if the current @see User has authority to show an audit
     * @param auditId The @see Audit id
     * @param shareCode The optional @see Audit share code
     * @return True if the current @see User has authority
     */
    boolean currentUserCanShowAudit(long auditId, String shareCode);

    /**
     * Check if the current @see User has authority to show an audit
     * @param audit The @see Audit
     * @param shareCode The optional @see Audit share code
     * @return True if the current @see User has authority
     */
    boolean currentUserCanShowAudit(Audit audit, String shareCode);

    /**
     * Check if the current @see User has authority for @see AuditScheduler on a given @see Audit id
     * @param auditId The @see Audit id
     * @return True if the @see User has access
     */
    boolean currentUserCanScheduleOnAudit(long auditId);

    /**
     * Change the @see User password
     * @param user The @see User
     * @param password The password
     * @return The new @see User
     */
    User changeUserPassword(User user, String password);
    
    /**
     * Get the user's details from their email
     * @param email
     * @return user details
     */
    UserDetails loadUserByEmail(String email);
}
