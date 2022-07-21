package com.tanaguru.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.user.ApiKey;
import com.tanaguru.domain.entity.membership.user.User;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByUserAndProject(User user, Project project);
    
    Optional<ApiKey> findByKey(String key);
    
}
