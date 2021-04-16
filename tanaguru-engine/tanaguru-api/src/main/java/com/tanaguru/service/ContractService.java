package com.tanaguru.service;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.EContractRole;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.contract.ContractRole;
import com.tanaguru.domain.entity.membership.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Date;

public interface ContractService {
    /**
     * Find authorities for a given @see EContractRole
     *
     * @param contractRole The given @see EContractRole
     * @return A list of String containing authority names
     */
    Collection<String> getRoleAuthorities(EContractRole contractRole);

    /**
     * Find authorities for a given @see EAppRole
     *
     * @param appRole The given @see EAppRole
     * @return A list of String containing authority names
     */
    Collection<String> getRoleAuthorities(EAppRole appRole);

    /**
     * Check if a @see User has a given authority on a @see Contract
     *
     * @param user          The @see User
     * @param authority     The authority to check
     * @param contract      The @see Contract to check authority on
     * @param checkOverride Check for role override
     * @return True if the @see User has the given authority on the @see Contract
     */
    boolean hasAuthority(User user, String authority, Contract contract, boolean checkOverride);

    /**
     * Check if a @see User has a given authority
     *
     * @param user      The @see User
     * @param authority The authority to check
     * @return True if the @see User has the given authority
     */
    boolean hasOverrideAuthority(User user, String authority);

    /**
     * Create a @see Contract for the given @see User
     *
     * @param owner       The @see User that owns the contract
     * @param name        The name of the @see contract
     * @param auditLimit  The limit of audit for the contract
     * @param projectLimit  The limit of @Project for the contract
     * @param restrictDomain True if audits urls must be restricted to each projects domain
     * @param contractEnd The end date of the contract
     * @return The new @see Contract
     */
    Contract createContract(User owner, String name, int auditLimit, int projectLimit, boolean restrictDomain, Date contractEnd);

    /**
     *
     * @param contract The @see Contract to modify
     * @param owner The new Owner of the @see Contract
     * @param name The new name
     * @param auditLimit The new AuditLimit
     * @param projectLimit  The limit of @Project for the contract
     * @param contractEnd The new @see Contract end date
     * @param isRestrictDomain  True if enable domain restriction on projects
     * @return The modified @see Contract
     */
    Contract modifyContract(Contract contract, User owner, String name, int auditLimit, int projectLimit, Date contractEnd, boolean isRestrictDomain);

    /**
     * Delete a given @see Cotnract
     * @param contract The @see Contract
     */
    void deleteContract(Contract contract);

    /**
     * Get all @see Contract for a given @see User and containing the name
     *
     * @param user The given @see User
     * @return A list of @see Contract
     */
    Page<Contract> findByUserAndContractName(String contractName, User user, Pageable pageable);

    /**
     * Get all @see Contract for a given @see User
     *
     * @param user The given @see User
     * @return A list of @see Contract
     */
    Page<Contract> findByUser(User user, Pageable pageable);
    
    /**
     * Find all contracts an @see User owns
     *
     * @param user The @see User that owns @see Contract
     * @return A collection of @see Contract
     */
    Collection<Contract> findByOwner(User user);

    /**
     * Find a @see ContractRole for a given @see EContractRole
     *
     * @param contractRole The @see EContractRole to find
     * @return the @see ContractRole
     */
    ContractRole getContractRole(EContractRole contractRole);

    /**
     * Add an @see User to a @see Contract
     * @param user The @see User to add
     * @param contract The @see targeted contract
     */
    ContractAppUser addMember(Contract contract, User user);

    /**
     * Delete a @see ContractAppUser
     * @param user The @see User to remove from @see Contract
     * @param contract The @see Contract to remove the @see User from
     */
    void removeMember(Contract contract, User user);
}
