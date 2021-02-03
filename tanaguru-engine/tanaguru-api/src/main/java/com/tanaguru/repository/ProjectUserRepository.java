package com.tanaguru.repository;

import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectAppUser, Long> {
    Optional<ProjectAppUser> findByProjectAndContractAppUser_User(Project project, User user);

    Collection<ProjectAppUser> findAllByProject(Project project);
    Collection<ProjectAppUser> findAllByProject_Id(long id);

    Collection<ProjectAppUser> findAllByContractAndContractAppUser_User(Contract contract, User user);

    Collection<ProjectAppUser> findAllByContractAppUser_User(User user);

    void deleteAllByProject(Project project);
}
