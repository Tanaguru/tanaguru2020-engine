package service.impl;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.EProjectRole;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.project.Project;
import com.tanaguru.domain.entity.membership.project.ProjectAppUser;
import com.tanaguru.domain.entity.membership.project.ProjectAuthority;
import com.tanaguru.domain.entity.membership.project.ProjectRole;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.repository.*;
import com.tanaguru.service.impl.ProjectServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceImplTest {

    @Mock
    private AppRoleRepository appRoleRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ContractUserRepository contractUserRepository;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @Mock
    private ProjectRoleRepository projectRoleRepository;

    @Mock
    private ActRepository actRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User user;
    private User otherUser;
    private Contract contract;
    private ProjectRole projectRole;
    private ProjectAuthority projectAuthority1;
    private ProjectAuthority projectAuthority2;
    private ProjectAuthority projectAuthority3;
    private Project project;
    private ContractAppUser contractAppUser;
    private ContractAppUser otherContractAppUser;
    private ProjectAppUser projectAppUser;

    @Before
    public void setup() {
        user = new User();
        user.setId(0L);

        contract = new Contract();

        projectRole = new ProjectRole();
        projectRole.setName(EProjectRole.PROJECT_MANAGER);

        projectAuthority1 = new ProjectAuthority();
        projectAuthority1.setName("test1");
        projectAuthority2 = new ProjectAuthority();
        projectAuthority1.setName("test2");
        projectAuthority3 = new ProjectAuthority();
        projectAuthority1.setName("test3");
        projectRole.setAuthorities(Arrays.asList(projectAuthority1, projectAuthority2, projectAuthority3));

        project = new Project();
        project.setName("test");
        project.setContract(contract);

        contractAppUser = new ContractAppUser();
        contractAppUser.setUser(user);
        contractAppUser.setContract(contract);

        otherUser = new User();
        otherUser.setId(1L);
        otherContractAppUser = new ContractAppUser();
        otherContractAppUser.setUser(otherUser);


        projectAppUser = new ProjectAppUser();
        projectAppUser.setContractAppUser(contractAppUser);
        projectAppUser.setProjectRole(projectRole);
        projectAppUser.setProject(project);

        ProjectRole override = new ProjectRole();
        override.setName(EProjectRole.PROJECT_GUEST);
        override.setAuthorities(new ArrayList<>());

        AppRole appRole = new AppRole();
        appRole.setName(EAppRole.ADMIN);
        appRole.setOverrideProjectRole(override);
        user.setAppRole(appRole);
    }

    @Test
    public void hasAuthorityTest_CheckOverrideFalseNoAuthorityNotOwner() {
        Mockito.when(projectUserRepository.findByProjectAndContractAppUser_User(Mockito.any(Project.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(projectAppUser));

        Mockito.when(contractUserRepository.findByContractAndContractRoleName_Owner(contract))
            .thenReturn(otherContractAppUser );

        Assert.assertFalse(projectService.hasAuthority(user, "test4", project, false));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideFalseOwner() {
        Mockito.when(contractUserRepository.findByContractAndContractRoleName_Owner(contract))
                .thenReturn(contractAppUser );

        Assert.assertTrue(projectService.hasAuthority(user, "test4", project, false));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideFalseWithAuthority() {
        Map<EProjectRole, Collection<String>> projectRoleAuthorityMap = projectService.getProjectRoleAuthorityMap();
        projectRoleAuthorityMap.put(EProjectRole.PROJECT_MANAGER, Collections.singletonList("test1"));

        Mockito.when(contractUserRepository.findByContractAndContractRoleName_Owner(contract))
                .thenReturn(otherContractAppUser );

        Mockito.when(projectUserRepository.findByProjectAndContractAppUser_User(Mockito.any(Project.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(projectAppUser));

        Assert.assertTrue(projectService.hasAuthority(user, "test1", project, false));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideTrueWithoutAuthority_WithProjectAuthority() {
        Map<EProjectRole, Collection<String>> projectRoleAuthorityMap = projectService.getProjectRoleAuthorityMap();
        projectRoleAuthorityMap.put(EProjectRole.PROJECT_MANAGER, Collections.singletonList("test1"));

        Mockito.when(contractUserRepository.findByContractAndContractRoleName_Owner(contract))
                .thenReturn(otherContractAppUser );

        Mockito.when(projectUserRepository.findByProjectAndContractAppUser_User(Mockito.any(Project.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(projectAppUser));

        Assert.assertTrue(projectService.hasAuthority(user, "test1", project, true));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideTrueWithoutAuthority_WithoutProjectAuthority() {
        Map<EProjectRole, Collection<String>> projectRoleAuthorityMap = projectService.getProjectRoleAuthorityMap();
        projectRoleAuthorityMap.put(EProjectRole.PROJECT_MANAGER, Collections.singletonList("test1"));

        Mockito.when(contractUserRepository.findByContractAndContractRoleName_Owner(contract))
                .thenReturn(otherContractAppUser );

        Mockito.when(projectUserRepository.findByProjectAndContractAppUser_User(Mockito.any(Project.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(projectAppUser));

        Assert.assertFalse(projectService.hasAuthority(user, "test4", project, true));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideTrueWithAuthority_WithoutProjectAuthority() {
        Map<EProjectRole, Collection<String>> projectRoleAuthorityMap = projectService.getProjectRoleAuthorityMap();
        projectRoleAuthorityMap.put(EProjectRole.PROJECT_MANAGER, Collections.singletonList("test1"));

        Mockito.when(contractUserRepository.findByContractAndContractRoleName_Owner(contract))
                .thenReturn(otherContractAppUser );

        Mockito.when(projectUserRepository.findByProjectAndContractAppUser_User(Mockito.any(Project.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(projectAppUser));

        Assert.assertTrue(projectService.hasAuthority(user, "test1", project, true));
    }
}
