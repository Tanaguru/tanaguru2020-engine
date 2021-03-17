package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.Contract;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author rcharre
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Page<Contract> findAll(Pageable pageable);
    Page<Contract> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
