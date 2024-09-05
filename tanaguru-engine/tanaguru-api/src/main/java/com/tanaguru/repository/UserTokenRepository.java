package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.entity.membership.user.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUser(User user);

    Optional<UserToken> findByToken(String token);

    void deleteByUser(User user);
}
