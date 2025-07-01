package com.programthis.user_service.service;

import com.programthis.userservice.model.User;
import com.programthis.userservice.service.UserService;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPassword("password123");
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ExistingId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NonExistingId_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void registerUser_NewUser_ShouldSaveAndReturnUser() {
        // Arrange
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerUser(testUser);

        // Assert
        assertEquals(testUser, result);
        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void registerUser_ExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.registerUser(testUser));
        assertEquals("El nombre de usuario ya existe.", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(testUser.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_ExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.registerUser(testUser));
        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ExistingUser_ShouldUpdateAndReturnUser() {
        // Arrange
        User updatedDetails = new User();
        updatedDetails.setUsername("newusername");
        updatedDetails.setEmail("new@example.com");
        updatedDetails.setFullName("New Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        Optional<User> result = userService.updateUser(1L, updatedDetails);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("newusername", result.get().getUsername());
        assertEquals("new@example.com", result.get().getEmail());
        assertEquals("New Name", result.get().getFullName());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_NonExistingUser_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.updateUser(1L, testUser);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void authenticateUser_ValidCredentials_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.authenticateUser("testuser", "password123");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void authenticateUser_InvalidUsername_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.authenticateUser("wronguser", "password123");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("wronguser");
    }

    @Test
    void authenticateUser_InvalidPassword_ShouldReturnEmpty() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.authenticateUser("testuser", "wrongpassword");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}