package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.EProjectRole;
import com.tanaguru.domain.entity.audit.Audit;
import com.tanaguru.domain.entity.membership.Act;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.project.ProjectAuthority;
import com.tanaguru.domain.entity.membership.project.ProjectRole;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomInvalidEntityException;
import com.tanaguru.repository.*;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    private final AppRoleRepository appRoleRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ActRepository actRepository;
    private final ContractUserRepository contractUserRepository;
    private final AuditService auditService;

    private Map<EProjectRole, ProjectRole> projectRoleMap = new EnumMap<>(EProjectRole.class);
    private Map<EProjectRole, Collection<String>> projectRoleAuthorityMap = new EnumMap<>(EProjectRole.class);
    private Map<EAppRole, Collection<String>> projectRoleAuthorityMapByAppRole = new EnumMap<>(EAppRole.class);

    @Autowired
    public ProjectServiceImpl(
            AppRoleRepository appRoleRepository, ProjectRepository projectRepository,
            ProjectUserRepository projectUserRepository,
            ProjectRoleRepository projectRoleRepository, ActRepository actRepository, ContractUserRepository contractUserRepository, AuditService auditService) {
        this.appRoleRepository = appRoleRepository;
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
        this.projectRoleRepository = projectRoleRepository;
        this.actRepository = actRepository;
        this.contractUserRepository = contractUserRepository;
        this.auditService = auditService;
    }

    @PostConstruct
    public void initMap() {
        for (ProjectRole projectRole : projectRoleRepository.findAll()) {
            Collection<String> authorities = projectRole.getAuthorities()
                    .stream()
                    .map(ProjectAuthority::getName)
                    .collect(Collectors.toList());
            projectRoleAuthorityMap.put(projectRole.getName(), authorities);
        }

        for (AppRole appRole : appRoleRepository.findAll()) {
            Collection<String> authorities = appRole.getOverrideProjectRole().getAuthorities()
                    .stream()
                    .map(ProjectAuthority::getName)
                    .collect(Collectors.toList());
            projectRoleAuthorityMapByAppRole.put(appRole.getName(), authorities);
        }

        for (EProjectRole projectRole : EProjectRole.values()) {
            projectRoleMap.put(projectRole, projectRoleRepository.findByName(projectRole)
                    .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_ROLE_NOT_FOUND, projectRole.toString() )));
        }
    }

    public Map<EProjectRole, ProjectRole> getProjectRoleMap() {
        return projectRoleMap;
    }

    public void setProjectRoleMap(Map<EProjectRole, ProjectRole> projectRoleMap) {
        this.projectRoleMap = projectRoleMap;
    }

    public Map<EProjectRole, Collection<String>> getProjectRoleAuthorityMap() {
        return projectRoleAuthorityMap;
    }

    public void setProjectRoleAuthorityMap(Map<EProjectRole, Collection<String>> projectRoleAuthorityMap) {
        this.projectRoleAuthorityMap = projectRoleAuthorityMap;
    }

    public Map<EAppRole, Collection<String>> getProjectRoleAuthorityMapByAppRole() {
        return projectRoleAuthorityMapByAppRole;
    }

    public void setProjectRoleAuthorityMapByAppRole(Map<EAppRole, Collection<String>> projectRoleAuthorityMapByAppRole) {
        this.projectRoleAuthorityMapByAppRole = projectRoleAuthorityMapByAppRole;
    }

    public Collection<String> getRoleAuthorities(EProjectRole projectRole) {
        return projectRoleAuthorityMap.getOrDefault(projectRole, new ArrayList<>());
    }

    public Collection<String> getRoleAuthorities(EAppRole appRole) {
        return projectRoleAuthorityMapByAppRole.getOrDefault(appRole, new ArrayList<>());
    }

    public ProjectRole getProjectRole(EProjectRole projectRole) {
        return projectRoleMap.get(projectRole);
    }

    public Project createProject(Contract contract, String name, String domain) {
        Project project = new Project();
        project.setContract(contract);
        project.setName(name);
        project.setDomain(domain);
        return projectRepository.save(project);
    }

    public Optional<Project> findByAudit(Audit audit) {
        return actRepository.findByAudit(audit).map(Act::getProject);
    }

    public boolean hasOverrideAuthority(User user, String authority) {
        return getRoleAuthorities(user.getAppRole().getName()).contains(authority);
    }

    @Override
    public Collection<Project> findAllByContractAndUser(Contract contract, User user) {
        return projectUserRepository.findAllByProject_ContractAndContractAppUser_User(contract, user)
                .stream().map(ProjectAppUser::getProject)
                .collect(Collectors.toList());
    }

    public Collection<String> getUserAuthoritiesOnProject(User user, Project project){
        ContractAppUser owner = contractUserRepository.findByContractAndContractRoleName_Owner(project.getContract());

        Collection<String> projectAuthorities =
                owner.getUser().getId() == user.getId() ?
                        getRoleAuthorities(EProjectRole.PROJECT_MANAGER) :

        projectUserRepository.findByProjectAndContractAppUser_User(project, user)
                .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND_FOR_PROJECT, String.valueOf(user.getId()) , String.valueOf(project.getId())  ))
                .getProjectRole().getAuthorities().stream()
                .map((ProjectAuthority::getName))
                .collect(Collectors.toList());

        //Add override authorities
        projectAuthorities.addAll(
                user.getAppRole().getOverrideProjectRole().getAuthorities().stream()
                        .map(ProjectAuthority::getName).collect(Collectors.toList()));

        return projectAuthorities;
    }

    public boolean hasAuthority(User user, String authority, Project project, boolean checkOverride) {
        boolean result = checkOverride && hasOverrideAuthority(user, authority);
        if (!result) {
            //Check if the user is the contract owner
            ContractAppUser target = contractUserRepository.findByContractAndContractRoleName_Owner(project.getContract());
            if(user.getId() == target.getUser().getId()){
                result = true;
            }else{
                Optional<ProjectAppUser> projectAppUser = projectUserRepository.findByProjectAndContractAppUser_User(project, user);
                result = projectAppUser.isPresent() &&
                        getRoleAuthorities(projectAppUser.get().getProjectRole().getName())
                                .contains(authority);
            }
        }
        return result;
    }

    public ProjectAppUser addMember(Project project, User user){
        if(!projectUserRepository.findByProjectAndContractAppUser_User(project, user).isPresent()){
            ContractAppUser contractAppUser = contractUserRepository.findByContractAndUser(project.getContract(), user)
                    .orElseThrow(() -> new CustomInvalidEntityException(CustomError.USER_NOT_FOUND_FOR_CONTRACT, String.valueOf(user.getId()) , String.valueOf(project.getContract().getId()) ));
            ProjectAppUser projectAppUser = new ProjectAppUser();
            projectAppUser.setContractAppUser(contractAppUser);
            projectAppUser.setProject(project);
            projectAppUser.setProjectRole(getProjectRole(EProjectRole.PROJECT_GUEST));
            return projectUserRepository.save(projectAppUser);
        }else{
            return null;
        }
    }

    public void removeMember(Project project, User user){
        ProjectAppUser projectAppUser = projectUserRepository.findByProjectAndContractAppUser_User(project, user)
                .orElseThrow(() -> new CustomInvalidEntityException(CustomError.USER_NOT_FOUND_FOR_PROJECT, String.valueOf(user.getId()) , String.valueOf(project.getId()) ));
        projectUserRepository.delete(projectAppUser);
    }

    public void deleteByContract(Contract contract){
        for(Project project : contract.getProjects()){
            deleteProject(project);
        }
    }

    public void deleteProject(Project project){
        auditService.deleteAuditByProject(project);
        projectRepository.deleteById(project.getId());
    }
}
