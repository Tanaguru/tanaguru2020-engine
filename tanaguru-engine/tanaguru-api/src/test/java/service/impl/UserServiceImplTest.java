package service.impl;

import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.InvalidEntityException;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.service.impl.UserServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void checkUsernameIsUsedTest_true() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.of(new User()));
        Assert.assertTrue(userService.checkUsernameIsUsed("test"));
    }

    @Test
    public void checkUsernameIsUsedTest_false() {
        Mockito.when(userRepository.findByUsername("test")).thenReturn(Optional.empty());
        Assert.assertFalse(userService.checkUsernameIsUsed("test"));
    }

    @Test
    public void checkEmailIsUsedTest_true() {
        Mockito.when(userRepository.findByEmail("test")).thenReturn(Optional.of(new User()));
        Assert.assertTrue(userService.checkEmailIsUsed("test"));
    }

    @Test
    public void checkEmailIsUsedTest_false() {
        Mockito.when(userRepository.findByEmail("test")).thenReturn(Optional.empty());
        Assert.assertFalse(userService.checkEmailIsUsed("test"));
    }


    @Test(expected = InvalidEntityException.class)
    public void modifyUser_invalidUsernameExists() {
        User from = new User();
        User to = new User();

        from.setUsername("test");
        to.setUsername("test2");

        Mockito.when(userRepository.findByUsername(to.getUsername())).thenReturn(Optional.of(to));
        userService.modifyUser(from, to);
    }

    @Test(expected = InvalidEntityException.class)
    public void modifyUser_invalidEmailExists() {
        User from = new User();
        User to = new User();

        from.setUsername("test");
        to.setUsername("test2");

        from.setEmail("test@test.com");
        to.setEmail("test2@test.com");

        Mockito.when(userRepository.findByUsername(to.getUsername())).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail(to.getEmail())).thenReturn(Optional.of(to));

        userService.modifyUser(from, to);
    }
}
