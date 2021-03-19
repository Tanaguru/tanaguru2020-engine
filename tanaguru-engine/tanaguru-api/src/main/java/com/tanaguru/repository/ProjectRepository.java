package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author rcharre
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Collection<Project> findAllByContract(Contract contract);
    Page<Project> findAllByContract(Contract contract, Pageable pageable);
    org.springframework.data.domain.Page<Project> findAllByContractInAndNameContaining(Collection<Contract> contracts, String name, Pageable pageable);

    /**
     * Find a page of shared project for a contract
     * @param contracts contracts list
     * @param pageable the page parameter
     * @return A page of projects
     */
    @Query(value = "select p from Project p where p.contract in :contracts and UPPER(p.name) LIKE CONCAT('%',UPPER(:search),'%') and 0 < (select count(pu) from ProjectAppUser pu where p=pu.project and pu.contractAppUser <> (select cu from ContractAppUser cu inner join ContractRole cr ON cu.contractRole=cr where cu.contract=p.contract and cr.name='CONTRACT_OWNER'))")
    org.springframework.data.domain.Page<Project> findSharedProject(@Param("contracts") Collection<Contract> contracts, @Param("search") String search, Pageable pageable);
}
