package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    /**
     * Find app role by name
     *
     * @param appRole The role to look for
     * @return The @see AppRole
     */
    Optional<AppRole> findByName(EAppRole appRole);
}
