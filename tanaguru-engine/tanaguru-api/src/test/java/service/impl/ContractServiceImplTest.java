package service.impl;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.constant.EContractRole;
import com.tanaguru.domain.entity.membership.contract.Contract;
import com.tanaguru.domain.entity.membership.contract.ContractAppUser;
import com.tanaguru.domain.entity.membership.contract.ContractAuthority;
import com.tanaguru.domain.entity.membership.contract.ContractRole;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.repository.AppRoleRepository;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.ContractRoleRepository;
import com.tanaguru.repository.ContractUserRepository;
import com.tanaguru.service.impl.ContractServiceImpl;
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
public class ContractServiceImplTest {
    @Mock
    private AppRoleRepository appRoleRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ContractUserRepository contractUserRepository;

    @Mock
    private ContractRoleRepository contractRoleRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    private User user;
    private ContractRole contractRole;
    private ContractAuthority contractAuthority1;
    private ContractAuthority contractAuthority2;
    private ContractAuthority contractAuthority3;
    private Contract contract;
    private ContractAppUser contractAppUser;

    @Before
    public void setup() {
        user = new User();
        contractRole = new ContractRole();
        contractRole.setName(EContractRole.CONTRACT_MANAGER);

        contractAuthority1 = new ContractAuthority();
        contractAuthority1.setName("test1");
        contractAuthority2 = new ContractAuthority();
        contractAuthority1.setName("test2");
        contractAuthority3 = new ContractAuthority();
        contractAuthority1.setName("test3");
        contractRole.setAuthorities(Arrays.asList(contractAuthority1, contractAuthority2, contractAuthority3));

        contract = new Contract();
        contract.setName("test");

        contractAppUser = new ContractAppUser();
        contractAppUser.setUser(user);

        contractAppUser = new ContractAppUser();
        contractAppUser.setContractRole(contractRole);
        contractAppUser.setContract(contract);

        ContractRole override = new ContractRole();
        override.setName(EContractRole.CONTRACT_GUEST);
        override.setAuthorities(new ArrayList<>());

        AppRole appRole = new AppRole();
        appRole.setName(EAppRole.ADMIN);
        appRole.setOverrideContractRole(override);
        user.setAppRole(appRole);
    }

    @Test
    public void hasAuthorityTest_CheckOverrideFalseNoAuthority() {
        Mockito.when(contractUserRepository.findByContractAndUser(Mockito.any(Contract.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(contractAppUser));

        Assert.assertFalse(contractService.hasAuthority(user, "test4", contract, false));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideFalseWithAuthority() {
        Map<EContractRole, Collection<String>> contractRoleAuthorityMap = contractService.getContractRoleAuthorityMap();
        contractRoleAuthorityMap.put(EContractRole.CONTRACT_MANAGER, Collections.singletonList("test1"));

        Mockito.when(contractUserRepository.findByContractAndUser(Mockito.any(Contract.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(contractAppUser));

        Assert.assertTrue(contractService.hasAuthority(user, "test1", contract, false));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideTrueWithoutAuthority_WithContractAuthority() {
        Map<EContractRole, Collection<String>> contractRoleAuthorityMap = contractService.getContractRoleAuthorityMap();
        contractRoleAuthorityMap.put(EContractRole.CONTRACT_MANAGER, Collections.singletonList("test1"));

        Mockito.when(contractUserRepository.findByContractAndUser(Mockito.any(Contract.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(contractAppUser));

        Assert.assertTrue(contractService.hasAuthority(user, "test1", contract, true));
    }

    @Test
    public void hasAuthorityTest_CheckOverrideTrueWithoutAuthority_WithoutContractAuthority() {
        Map<EContractRole, Collection<String>> contractRoleAuthorityMap = contractService.getContractRoleAuthorityMap();
        contractRoleAuthorityMap.put(EContractRole.CONTRACT_MANAGER, Collections.singletonList("test1"));

        Mockito.when(contractUserRepository.findByContractAndUser(Mockito.any(Contract.class), Mockito.any(User.class)))
                .thenReturn(Optional.of(contractAppUser));

        Assert.assertFalse(contractService.hasAuthority(user, "test4", contract, true));
    }
}
