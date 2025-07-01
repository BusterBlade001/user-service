package com.programthis.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.programthis.userservice.model.User;
import com.programthis.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password123", "test@example.com", "Test User");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getUserById_NonExistingUser_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerUser_ValidUser_ShouldReturnCreated() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void registerUser_ExistingUser_ShouldReturnBadRequest() throws Exception {
        when(userService.registerUser(any(User.class))).thenThrow(new RuntimeException("El nombre de usuario ya existe."));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El nombre de usuario ya existe."));
    }
    
    @Test
    void updateUser_ExistingUser_ShouldReturnUpdatedUser() throws Exception {
        User updatedDetails = new User(1L, "newuser", "newpass", "new@example.com", "New User");
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(Optional.of(updatedDetails));

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void updateUser_NonExistingUser_ShouldReturnNotFound() throws Exception {
        User updatedDetails = new User(1L, "newuser", "newpass", "new@example.com", "New User");
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void login_ValidCredentials_ShouldReturnSuccessMessage() throws Exception {
        UserController.LoginRequest loginRequest = new UserController.LoginRequest("testuser", "password123");
        when(userService.authenticateUser("testuser", "password123")).thenReturn(Optional.of(testUser));
    
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Inicio de sesión exitoso para testuser"));
    }
    
    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        UserController.LoginRequest loginRequest = new UserController.LoginRequest("testuser", "wrongpassword");
        when(userService.authenticateUser("testuser", "wrongpassword")).thenReturn(Optional.empty());
    
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales inválidas"));
    }
}