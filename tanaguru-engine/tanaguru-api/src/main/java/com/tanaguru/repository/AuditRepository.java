package com.tanaguru.repository;

import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.entity.audit.Audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Stream;
import javax.persistence.QueryHint;


/**
 * @author rcharre
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    org.springframework.data.domain.Page<Audit> findAll(Pageable pageable);
    Collection<Audit> findAllByDeletedIsTrue();
    
    @Query(value = "SELECT count(*) FROM Audit WHERE type = :type ")
    int numberOfAuditByType(@Param("type") EAuditType type);
    
    @Query(value = "SELECT count(*) FROM Audit WHERE type = :type AND dateStart>:startDate AND dateStart<:endDate")
    int numberOfAuditByTypeAndPeriod(@Param("type") EAuditType type, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("select a from Audit a")
    Stream<Audit> getAll();
    
    Page<Audit> findAllByIsPrivateIsFalse(Pageable pageable);
}
