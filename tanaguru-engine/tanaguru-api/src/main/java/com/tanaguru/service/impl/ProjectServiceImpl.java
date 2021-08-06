package com.tanaguru.service.impl;

import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.EAuditType;
import com.tanaguru.domain.constant.EContractRole;
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
import com.tanaguru.service.AsyncAuditService;
import com.tanaguru.service.AuditService;
import com.tanaguru.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final AppRoleRepository appRoleRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ActRepository actRepository;
    private final ContractUserRepository contractUserRepository;
    private final AuditService auditService;
    private final AsyncAuditService asyncAuditService;

    private Map<EProjectRole, ProjectRole> projectRoleMap = new EnumMap<>(EProjectRole.class);
    private Map<EProjectRole, Collection<String>> projectRoleAuthorityMap = new EnumMap<>(EProjectRole.class);
    private Map<EAppRole, Collection<String>> projectRoleAuthorityMapByAppRole = new EnumMap<>(EAppRole.class);

    @Autowired
    public ProjectServiceImpl(
            AppRoleRepository appRoleRepository, ProjectRepository projectRepository,
            ProjectUserRepository projectUserRepository,
            ProjectRoleRepository projectRoleRepository, ActRepository actRepository, ContractUserRepository contractUserRepository, AuditService auditService, AsyncAuditService asyncAuditService) {
        this.appRoleRepository = appRoleRepository;
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
        this.projectRoleRepository = projectRoleRepository;
        this.actRepository = actRepository;
        this.contractUserRepository = contractUserRepository;
        this.auditService = auditService;
        this.asyncAuditService = asyncAuditService;
    }

    @PostConstruct
    public void initMap() {
        LOGGER.debug("Initialize project role authorities map");
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
                    .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.PROJECT_ROLE_NOT_FOUND, projectRole.toString())));
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

    public Project createProject(
            Contract contract, 
            String name, 
            String domain, 
            boolean allowPageAudit, 
            boolean allowSiteAudit,
            boolean allowScenarioAudit,
            boolean allowUploadAudit) {
        LOGGER.info("Create project {} for contract {}", name, contract.getId());
        Project project = new Project();
        project.setContract(contract);
        project.setName(name);
        project.setDomain(domain);
        project.setAllowPageAudit(allowPageAudit);
        project.setAllowSiteAudit(allowSiteAudit);
        project.setAllowScenarioAudit(allowScenarioAudit);
        project.setAllowUploadAudit(allowUploadAudit);
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

    @Override
    public Page<Project> findPageByContractAndUser(Contract contract, User user, Pageable pageable) {
        Page<ProjectAppUser> appUsers = projectUserRepository.findAllByProject_ContractAndContractAppUser_User(contract, user, pageable);
        List<Project> projects = appUsers.toList()
                .stream().map(ProjectAppUser::getProject)
                .collect(Collectors.toList());
        return new PageImpl<>(projects, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), appUsers.getTotalElements());
    }

    @Override
    public Collection<Project> findAllByUserMemberOfNotOwner(User user) {
        return projectUserRepository.findAllByContractAppUser_User(user)
                .stream()
                .filter(projectAppUser -> projectAppUser.getContractAppUser().getContractRole().getName() != EContractRole.CONTRACT_OWNER)
                .map(ProjectAppUser::getProject)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Project> findPageByUserMemberOfNotOwner(User user, String search, Pageable pageable) {
        Page<ProjectAppUser> projects = projectUserRepository.findSharedWith(user, search, pageable);
        List<Project> projectsList = projects.toList()
                .stream()
                .filter(projectAppUser -> projectAppUser.getContractAppUser().getContractRole().getName() != EContractRole.CONTRACT_OWNER)
                .map(ProjectAppUser::getProject)
                .collect(Collectors.toList());
        return new PageImpl<>(
                projectsList,
                pageable,
                projects.getTotalElements()
        );

    }

    public Collection<String> getUserAuthoritiesOnProject(User user, Project project) {
        ContractAppUser owner = contractUserRepository.findByContractAndContractRoleName_Owner(project.getContract());

        Collection<String> projectAuthorities =
                owner.getUser().getId() == user.getId() ?
                        new ArrayList<>(getRoleAuthorities(EProjectRole.PROJECT_MANAGER)) :
                        new ArrayList<>();

        Optional<ProjectAppUser> projectUser = projectUserRepository.findByProjectAndContractAppUser_User(project, user);
        projectUser.ifPresent(projectAppUser -> projectAuthorities.addAll(
                projectAppUser.getProjectRole().getAuthorities().stream()
                        .map((ProjectAuthority::getName))
                        .collect(Collectors.toList())));

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
            if (user.getId() == target.getUser().getId()) {
                result = true;
            } else {
                Optional<ProjectAppUser> projectAppUser = projectUserRepository.findByProjectAndContractAppUser_User(project, user);
                result = projectAppUser.isPresent() &&
                        getRoleAuthorities(projectAppUser.get().getProjectRole().getName())
                                .contains(authority);
            }
        }
        return result;
    }

    public ProjectAppUser addMember(Project project, User user) {
        if (!projectUserRepository.findByProjectAndContractAppUser_User(project, user).isPresent()) {
            ContractAppUser contractAppUser = contractUserRepository.findByContractAndUser(project.getContract(), user)
                    .orElseThrow(() -> new CustomInvalidEntityException(CustomError.USER_NOT_FOUND_FOR_CONTRACT, String.valueOf(user.getId()), String.valueOf(project.getContract().getId())));
            ProjectAppUser projectAppUser = new ProjectAppUser();
            projectAppUser.setContractAppUser(contractAppUser);
            projectAppUser.setProject(project);
            projectAppUser.setProjectRole(getProjectRole(EProjectRole.PROJECT_GUEST));
            LOGGER.info("[Project {}] Add user {}", project.getId(), projectAppUser.getContractAppUser().getUser().getId());
            return projectUserRepository.save(projectAppUser);
        } else {
            return null;
        }
    }

    public void removeMember(Project project, User user) {
        ProjectAppUser projectAppUser = projectUserRepository.findByProjectAndContractAppUser_User(project, user)
                .orElseThrow(() -> new CustomInvalidEntityException(CustomError.USER_NOT_FOUND_FOR_PROJECT, String.valueOf(user.getId()), String.valueOf(project.getId())));
        LOGGER.info("[Project {}] remove user {}", project.getId(), projectAppUser.getContractAppUser().getUser().getId());
        projectUserRepository.delete(projectAppUser);
    }

    public void deleteByContract(Contract contract) {
        LOGGER.info("Delete all projects for contract {}", contract.getId());
        contract.getProjects()
                .forEach(this::deleteProject);
    }

    public void deleteProject(Project project) {
        LOGGER.info("[Project {}] delete", project.getId());
        actRepository.deleteAllByProject(project);
        projectUserRepository.deleteAllByProject(project);
        projectRepository.deleteById(project.getId());

        auditService.findAllByProject(project)
                .forEach(asyncAuditService::deleteAudit);
    }

    public Project modifyProject(Project project, String name, String domain) {
        LOGGER.info("[Project {}] modify", project.getId());
        project.setName(name);
        project.setDomain(domain);
        return projectRepository.save(project);
    }

    @Override
    public boolean projectAcceptThisAuditType(EAuditType auditType, Project project) {
        boolean projectAllowThisTypeOfAudit = false;
        if(auditType.equals(EAuditType.PAGE)) {
            projectAllowThisTypeOfAudit = project.isAllowPageAudit();
        }else if(auditType.equals(EAuditType.SITE)) {
            projectAllowThisTypeOfAudit = project.isAllowSiteAudit();
        }else if(auditType.equals(EAuditType.SCENARIO)) {
            projectAllowThisTypeOfAudit = project.isAllowScenarioAudit();
        }else if(auditType.equals(EAuditType.UPLOAD)) {
            projectAllowThisTypeOfAudit = project.isAllowUploadAudit();
        }
        return projectAllowThisTypeOfAudit;
    }
}
