package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Scenario;
import com.tanaguru.domain.entity.membership.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Collection<Scenario> findAllByProjectAndIsDeletedIsFalse(Project project);
    Collection<Scenario> findAllByProject_IdAndIsDeletedIsFalse(long projectId);
}