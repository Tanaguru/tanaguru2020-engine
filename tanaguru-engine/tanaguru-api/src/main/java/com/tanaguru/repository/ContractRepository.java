package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**=
 * @author rcharre
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Page<Contract> findAll(Pageable pageable);
    Page<Contract> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Contract> findById(Long id);

    @Query("select c from Contract c " +
            "inner join ContractAppUser cu ON cu.contract=c " +
            "where UPPER(c.name) LIKE CONCAT('%',UPPER(:search),'%') and " +
            "cu.user=:user ")
    Page<Contract> findAllByName_AndContractAppUsers_User(@Param("search") String search, @Param("user") User user, Pageable pageable);

    @Query("select c from Contract c " +
            "inner join ContractAppUser cu ON cu.contract=c " +
            "where cu.user=:user ")
    Page<Contract> findAllByContractAppUsers_User(@Param("user") User user, Pageable pageable);
}
