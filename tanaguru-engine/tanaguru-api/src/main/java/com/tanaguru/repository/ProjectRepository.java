package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.project.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author rcharre
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Collection<Project> findAllByContract(Contract contract);
    org.springframework.data.domain.Page<Project> findAllByContract(Contract contract, Pageable pageable);
}
