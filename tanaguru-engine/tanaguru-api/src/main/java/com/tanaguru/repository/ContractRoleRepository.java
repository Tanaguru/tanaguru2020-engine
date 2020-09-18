package com.tanaguru.repository;

import com.tanaguru.domain.constant.EContractRole;
import com.tanaguru.domain.entity.membership.contract.ContractRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ContractRoleRepository extends JpaRepository<ContractRole, Long> {
    Optional<ContractRole> findByName(EContractRole contractRole);
}
