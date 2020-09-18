package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.ContractAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ContractAuthorityRepository extends JpaRepository<ContractAuthority, Long> {
}
