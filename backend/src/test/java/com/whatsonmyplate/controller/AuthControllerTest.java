package com.whatsonmyplate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsonmyplate.dto.auth.AuthResponse;
import com.whatsonmyplate.dto.auth.LoginRequest;
import com.whatsonmyplate.dto.auth.RegisterRequest;
import com.whatsonmyplate.dto.auth.UserProfileDto;
import com.whatsonmyplate.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testHealthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("WhatsOnMyPlate"));
    }

    @Test
    void testRegisterEndpointIsPublic() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "newuser@example.com",
                "newuser",
                "Password123!",
                "New User"
        );

        AuthResponse mockResponse = new AuthResponse("mock.jwt.token", "Bearer", 86400000L);
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L));
    }

    @Test
    void testRegisterValidationFailure() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(
                "invalid-email",
                "",
                "short",
                ""
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testLoginEndpointIsPublic() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");
        AuthResponse mockResponse = new AuthResponse("mock.login.token", "Bearer", 86400000L);

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.login.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void testLoginFailureGenericResponse() throws Exception {
        LoginRequest request = new LoginRequest("user@example.com", "WrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password."));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void testGetMeWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testGetMeWithAuthenticationSucceeds() throws Exception {
        UserProfileDto mockProfile = new UserProfileDto(
                1L,
                "user@example.com",
                "username123",
                "User Name",
                "https://example.com/avatar.jpg",
                LocalDateTime.now()
        );

        when(authService.getCurrentUser("user@example.com")).thenReturn(mockProfile);

        mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.username").value("username123"))
                .andExpect(jsonPath("$.displayName").value("User Name"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void testArbitraryProtectedApiEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/pantry")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
