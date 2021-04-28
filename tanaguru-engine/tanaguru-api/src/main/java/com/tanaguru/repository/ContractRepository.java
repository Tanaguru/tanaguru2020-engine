package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Page<Contract> findAll(Pageable pageable);
    Page<Contract> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Contract> findById(Long id);

    Page<Contract> findAllByContractAppUsers_UserAndNameContainingIgnoreCase(User user, String contractName, Pageable pageable);

    Page<Contract> findAllByContractAppUsers_User(User user, Pageable pageable);
}
