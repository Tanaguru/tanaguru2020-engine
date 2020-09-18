package com.tanaguru.repository;

import com.tanaguru.domain.constant.EProjectRole;
import com.tanaguru.domain.entity.membership.project.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
    Optional<ProjectRole> findByName(EProjectRole projectRole);
}
