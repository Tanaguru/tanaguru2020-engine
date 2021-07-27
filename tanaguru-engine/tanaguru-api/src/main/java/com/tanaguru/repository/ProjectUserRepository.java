package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectAppUser, Long> {
    Optional<ProjectAppUser> findByProjectAndContractAppUser_User(Project project, User user);

    Collection<ProjectAppUser> findAllByProject(Project project);
    Collection<ProjectAppUser> findAllByProject_Id(long id);

    Collection<ProjectAppUser> findAllByProject_ContractAndContractAppUser_User(Contract contract, User user);

    Page<ProjectAppUser> findAllByProject_ContractAndContractAppUser_User(Contract contract, User user, Pageable pageable);

    Collection<ProjectAppUser> findAllByContractAppUser_User(User user);

    @Query("select pu from ProjectAppUser pu " +
            "where pu.contractAppUser.user=:user " +
            "and pu.contractAppUser.contractRole.name <> 'CONTRACT_OWNER' " +
            "and UPPER(pu.project.name) LIKE CONCAT('%',UPPER(:search),'%')")
    Page<ProjectAppUser> findSharedWith(@Param("user")User user, @Param("search")String search, Pageable pageable);

    void deleteAllByProject(Project project);
}
