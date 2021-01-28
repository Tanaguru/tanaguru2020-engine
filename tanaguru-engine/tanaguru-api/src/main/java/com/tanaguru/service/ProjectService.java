package com.tanaguru.service;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.EProjectRole;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.project.ProjectRole;
import com.tanaguru.domain.entity.membership.user.User;

import java.util.Collection;
import java.util.Optional;

public interface ProjectService {
    Project createProject(Contract contract, String name, String domain);

    /**
     * Find authorities for a given @see EProjectRole
     *
     * @param projectRole The given @see EProjectRole
     * @return A list of String containing authority names
     */
    Collection<String> getRoleAuthorities(EProjectRole projectRole);

    /**
     * Find authorities for a given @see EAppRole
     *
     * @param appRole The given @see EAppRole
     * @return A list of String containing authority names
     */
    Collection<String> getRoleAuthorities(EAppRole appRole);

    /**
     * Get a collection containing all @see User authorities on a given @see Project
     * @param user The given @see User
     * @param project The given @see Project
     * @return The authorities name collection
     */
    Collection<String> getUserAuthoritiesOnProject(User user, Project project);

    /**
     * Check if a @see User has a given authority on a @see Project
     *
     * @param user          The @see User
     * @param authority     The authority to check
     * @param project       The @see Project to check authority on
     * @param checkOverride Check for role override
     * @return True if the @see User has the given authority on the @see Project
     */
    boolean hasAuthority(User user, String authority, Project project, boolean checkOverride);

    /**
     * Check if a @see User has a given authority
     *
     * @param user      The @see User
     * @param authority The authority to check
     * @return True if the @see User has the given authority
     */
    boolean hasOverrideAuthority(User user, String authority);

    /**
     * Find a @see Project for a given @see Audit
     *
     * @param audit The @see Audit
     * @return The @see Project
     */
    Optional<Project> findByAudit(Audit audit);

    /**
     * @param contract The @see Contract
     * @param user     The @see User
     * @return A collection of @see Project
     */
    Collection<Project> findAllByContractAndUser(Contract contract, User user);

    /**
     * @param user     The @see User
     * @return A collection of @see Project
     */
    Collection<Project> findAllByUser(User user);

    /**
     *
     * @param user     The @see User
     * @return A collection of @see Project
     */
    Collection<Project> findAllByUserNotOwner(User user);

    /**
     * Find a @see ProjectRole for a given @see EProjectRole
     *
     * @param projectRole The @see EProjectRole to find
     * @return the @see ProjectRole
     */
    ProjectRole getProjectRole(EProjectRole projectRole);

    /**
     * Add an @see User to a @see Project
     * @param user The @see User to add
     * @param project The @see targeted project
     */
    ProjectAppUser addMember(Project project, User user);

    /**
     * Delete a @see ProjectAppUser
     * @param user The @see User to remove from @see Project
     * @param project The @see Project to remove the @see User from
     */
    void removeMember(Project project, User user);

    void deleteByContract(Contract contract);

    /**
     * Delete a given @see Project
     * @param project The given @see Project
     */
    void deleteProject(Project project);
}
