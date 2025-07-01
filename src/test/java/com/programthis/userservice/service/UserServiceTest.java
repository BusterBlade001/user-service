package com.programthis.userservice.service;

import com.programthis.userservice.model.User;
import com.programthis.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "busterblade", "password123", "buster@example.com", "Buster Blade");
        user2 = new User(2L, "testuser", "password456", "test@example.com", "Test User");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        Optional<User> user = userService.getUserById(1L);

        assertTrue(user.isPresent());
        assertEquals("busterblade", user.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> user = userService.getUserById(1L);

        assertFalse(user.isPresent());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void registerUser_ShouldReturnNewUser() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user1);

        User newUser = new User(null, "busterblade", "password123", "buster@example.com", "Buster Blade");
        User savedUser = userService.registerUser(newUser);

        assertNotNull(savedUser);
        assertEquals("busterblade", savedUser.getUsername());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.findByUsername("busterblade")).thenReturn(Optional.of(user1));

        User newUser = new User(null, "busterblade", "password123", "another@example.com", "Another User");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(newUser);
        });

        assertEquals("El nombre de usuario ya existe.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserExists() {
        User userDetails = new User(null, "updatedUser", "newpassword", "updated@example.com", "Updated User");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);

        Optional<User> updatedUser = userService.updateUser(1L, userDetails);

        assertTrue(updatedUser.isPresent());
        assertEquals("updatedUser", updatedUser.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user1);
    }

    @Test
    void deleteUser_ShouldInvokeDeleteById() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void authenticateUser_ShouldReturnUser_WhenCredentialsAreCorrect() {
        when(userRepository.findByUsername("busterblade")).thenReturn(Optional.of(user1));

        Optional<User> authenticatedUser = userService.authenticateUser("busterblade", "password123");

        assertTrue(authenticatedUser.isPresent());
        assertEquals("busterblade", authenticatedUser.get().getUsername());
    }

    @Test
    void authenticateUser_ShouldReturnEmpty_WhenPasswordIsIncorrect() {
        when(userRepository.findByUsername("busterblade")).thenReturn(Optional.of(user1));

        Optional<User> authenticatedUser = userService.authenticateUser("busterblade", "wrongpassword");

        assertFalse(authenticatedUser.isPresent());
    }
}