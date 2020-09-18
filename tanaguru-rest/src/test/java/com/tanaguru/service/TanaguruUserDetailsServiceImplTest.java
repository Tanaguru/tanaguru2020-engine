package com.tanaguru.service;

import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.entity.membership.user.AppAuthority;
import com.tanaguru.domain.entity.membership.user.AppRole;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.repository.AppRoleRepository;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.impl.TanaguruUserDetailsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TanaguruUserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AppRoleRepository appRoleRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private TanaguruUserDetailsServiceImpl userDetailsService;

    private Collection<String> authorities = Arrays.asList("testAuthority", "testAuthority2");
    private AppRole appRole;
    private User user;

    @Before
    public void initEntities() {
        appRole = new AppRole();
        appRole.setName(EAppRole.ADMIN);

        ArrayList<AppAuthority> appAuthorities = new ArrayList<>();
        for (String authority : authorities) {
            AppAuthority appAuthority = new AppAuthority();
            appAuthority.setName(authority);
            appAuthorities.add(appAuthority);
        }
        appRole.setAuthorities(appAuthorities);

        user = new User();
        user.setEnabled(true);
        user.setUsername("testUser");
        user.setEmail("testEmail@email.com");
        user.setAppRole(appRole);
        user.setPassword("test");
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsername_NotFound() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        userDetailsService.loadUserByUsername("test");
    }

    @Test()
    public void loadUserByUsername() {
        Mockito.when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        UserDetails userDetails = userDetailsService.loadUserByUsername("testUser");

        assertEquals("test", userDetails.getPassword());
        assertEquals("testUser", userDetails.getUsername());
        for (GrantedAuthority grantedAuthority : userDetails.getAuthorities()) {
            assertTrue(authorities.contains(grantedAuthority.getAuthority()));
        }
    }

    @Test
    public void setAdminUserTest_NotExists() {
        Mockito.when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        Mockito.when(appRoleRepository.findByName(EAppRole.SUPER_ADMIN)).thenReturn(Optional.of(appRole));
        userDetailsService.setAdminUser();
        verify(appRoleRepository, times(1)).findByName(EAppRole.SUPER_ADMIN);
    }

    @Test(expected = IllegalStateException.class)
    public void setAdminUserTest_NotExists_And_RoleNotExists() {
        Mockito.when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        Mockito.when(appRoleRepository.findByName(EAppRole.SUPER_ADMIN)).thenReturn(Optional.empty());
        userDetailsService.setAdminUser();
    }

    @Test
    public void setAdminUserTest_Exists() {
        Mockito.when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        userDetailsService.setAdminUser();
        verify(appRoleRepository, times(0)).findByName(EAppRole.SUPER_ADMIN);
    }
}
