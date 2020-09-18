package service.impl;

import com.tanaguru.domain.constant.AppAuthorityName;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.AppAuthority;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.repository.AppRoleRepository;
import com.tanaguru.service.impl.AppRoleServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AppRoleServiceImplTest {
    @Mock
    private AppRoleRepository appRoleRepository;

    @InjectMocks
    private AppRoleServiceImpl appRoleService;

    @Test(expected = IllegalStateException.class)
    public void initRoleMapTest_RespositoryReturnEmpty() {
        appRoleService.initRoleMap();
    }

    @Test
    public void getAppRole_NotExists() {
        AppAuthority appAuthority = new AppAuthority();
        appAuthority.setName(AppAuthorityName.PUBLIC_SCHEDULE_ACCESS);

        AppRole superAdmin = new AppRole();
        superAdmin.setAuthorities(Collections.singleton(appAuthority));

        AppRole admin = new AppRole();
        admin.setAuthorities(Collections.singleton(appAuthority));

        AppRole user = new AppRole();
        user.setAuthorities(Collections.singleton(appAuthority));

        Mockito.when(appRoleRepository.findByName(EAppRole.SUPER_ADMIN)).thenReturn(Optional.of(superAdmin));
        Mockito.when(appRoleRepository.findByName(EAppRole.ADMIN)).thenReturn(Optional.of(admin));
        Mockito.when(appRoleRepository.findByName(EAppRole.USER)).thenReturn(Optional.of(user));

        appRoleService.initRoleMap();
        assertFalse(appRoleService.getAppRole(null).isPresent());
    }

    @Test
    public void getAppRole_Exists() {
        AppAuthority appAuthority = new AppAuthority();
        appAuthority.setName(AppAuthorityName.PUBLIC_SCHEDULE_ACCESS);

        AppRole superAdmin = new AppRole();
        superAdmin.setAuthorities(Collections.singleton(appAuthority));

        AppRole admin = new AppRole();
        admin.setAuthorities(Collections.singleton(appAuthority));

        AppRole user = new AppRole();
        user.setAuthorities(Collections.singleton(appAuthority));

        Mockito.when(appRoleRepository.findByName(EAppRole.SUPER_ADMIN)).thenReturn(Optional.of(superAdmin));
        Mockito.when(appRoleRepository.findByName(EAppRole.ADMIN)).thenReturn(Optional.of(admin));
        Mockito.when(appRoleRepository.findByName(EAppRole.USER)).thenReturn(Optional.of(user));

        appRoleService.initRoleMap();
        assertTrue(appRoleService.getAppRole(EAppRole.SUPER_ADMIN).isPresent());
    }
}
