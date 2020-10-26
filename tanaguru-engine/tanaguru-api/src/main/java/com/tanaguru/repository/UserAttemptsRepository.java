package com.tanaguru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.entity.membership.user.UserAttempts;
import java.util.Optional;

@Repository
public interface UserAttemptsRepository extends JpaRepository<UserAttempts, Long>{
	
	Optional<UserAttempts> findByUsername(String username);

}
