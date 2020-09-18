package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.user.AppAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppAuthorityRepository extends JpaRepository<AppAuthority, Long> {
    Optional<AppAuthority> findByName(String name);
}
