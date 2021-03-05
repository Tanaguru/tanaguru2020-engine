package com.tanaguru.repository;

import com.tanaguru.domain.entity.audit.Resource;
import com.tanaguru.domain.entity.membership.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Collection<Resource> findAllByProjectAndIsDeletedIsFalse(Project project);
    Collection<Resource> findAllByProject_IdAndIsDeletedIsFalse(long projectId);
    Collection<Resource> findByIsDeletedIsFalse();
    Collection<Resource> findAllByIdIn(Collection<Long> ids);
}