package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAppAccountType;
import com.tanaguru.domain.entity.membership.user.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

/**
 * @author rcharre
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    org.springframework.data.domain.Page<User> findAll(Pageable pageable);
    
    org.springframework.data.domain.Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email, Pageable pageable);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Collection<User> findAllByAppAccountTypeNameAndExpired(EAppAccountType accountType, boolean expired);
    
}
