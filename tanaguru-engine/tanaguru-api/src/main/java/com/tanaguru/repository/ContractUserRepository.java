package com.tanaguru.repository;

import com.tanaguru.domain.constant.EContractRole;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.user.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractUserRepository extends JpaRepository<ContractAppUser, Long> {

    Optional<ContractAppUser> findByContractAndUser(Contract contract, User user);

    Collection<ContractAppUser> findAllByUser(User user);
    
    @Query("SELECT contractAppUser.contract from ContractAppUser as contractAppUser where contractAppUser.user = :user and contractAppUser.contract.name like %:contractName% ")
    Page<Contract> findAllContractByUserAndContractNameContaining(@Param("user") User user,@Param("contractName") String contractName, Pageable pageable);

    @Query("SELECT contractAppUser.contract from ContractAppUser as contractAppUser where contractAppUser.user = :user")
    Page<Contract> findAllContractByUser(@Param("user") User user, Pageable pageable);

    default ContractAppUser findByContractAndContractRoleName_Owner(Contract contract){
        return findAllByContractAndContractRoleName(contract, EContractRole.CONTRACT_OWNER).get(0);
    }

    List<ContractAppUser> findAllByContractAndContractRoleName(Contract contract, EContractRole contractRole);

    default Collection<ContractAppUser> findAllByUserAndContractRole_Name_Owner(User user) {
        return findAllByUserAndContractRole_Name(user, EContractRole.CONTRACT_OWNER);
    }

    List<ContractAppUser> findAllByContractAndUserAndContractRole_Name(Contract contract, User user, EContractRole contractRole);

    List<ContractAppUser> findAllByUserAndContractRole_Name(User user, EContractRole contractRole);

    Collection<ContractAppUser> findAllByContract(Contract contract);
    Page<ContractAppUser> findAllByContract_Id(long id, Pageable pageable);

    void deleteAllByContract(Contract contract);

}
