package com.tanaguru.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tanaguru.domain.constant.EAppAccountType;
import com.tanaguru.domain.entity.membership.user.AppAccountType;

@Repository
public interface AppAccountTypeRepository extends JpaRepository<AppAccountType, Long>{
    /**
     * Find app account type by name
     *
     * @param appAccountType The account type to look for
     * @return The @see AppAccountType
     */
    Optional<AppAccountType> findByName(EAppAccountType appAccountType);
}
