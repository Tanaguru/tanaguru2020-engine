package com.tanaguru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanaguru.domain.entity.membership.user.Connection;

/**
 * @author lpedrau
 */
@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

}
