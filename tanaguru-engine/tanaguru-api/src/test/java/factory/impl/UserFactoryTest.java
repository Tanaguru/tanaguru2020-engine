package factory.impl;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.factory.impl.UserFactoryImpl;
import com.tanaguru.repository.ContractRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.AppRoleService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class UserFactoryTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AppRoleService appRoleService;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private UserFactoryImpl userFactory;

    @Test(expected = CustomEntityNotFoundException.class)
    public void createUserTest_RoleNotFound() {
        Mockito.when(appRoleService.getAppRole(EAppRole.ADMIN)).thenReturn(Optional.empty());
        userFactory.createUser("test", "test@test.com", "test", EAppRole.ADMIN, false);
    }

    @Test
    public void createUserTest_RoleFound() {
        AppRole appRole = new AppRole();
        User user = new User();
        user.setAppRole(appRole);
        user.setUsername("test");
        user.setPassword("test");
        user.setEmail("test@test.com");

        Mockito.when(appRoleService.getAppRole(EAppRole.ADMIN)).thenReturn(Optional.of(appRole));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        User userRes = userFactory.createUser("test", "test@test.com", "test", EAppRole.ADMIN, false);
        Assert.assertEquals(userRes.getEmail(), "test@test.com");
        Assert.assertEquals(userRes.getPassword(), "test");
        Assert.assertEquals(userRes.getUsername(), "test");
    }
}
