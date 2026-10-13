package com.whatsonmyplate.service;

import com.whatsonmyplate.dto.auth.AuthResponse;
import com.whatsonmyplate.dto.auth.LoginRequest;
import com.whatsonmyplate.dto.auth.RegisterRequest;
import com.whatsonmyplate.dto.auth.UserProfileDto;
import com.whatsonmyplate.entity.User;
import com.whatsonmyplate.repository.UserRepository;
import com.whatsonmyplate.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void testRegisterSuccessWithBcryptHashing() {
        RegisterRequest request = new RegisterRequest("test@example.com", "testuser", "RawPassword123!", "Test User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("RawPassword123!")).thenReturn("$2a$10$hashedPasswordString");

        User savedUser = new User("testuser", "test@example.com", "$2a$10$hashedPasswordString", "Test User");
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken("test@example.com", 10L)).thenReturn("jwt.token.here");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("$2a$10$hashedPasswordString", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void testRegisterDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "newuser", "Password123!", "Name");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Email is already registered.", exception.getMessage());
    }

    @Test
    void testRegisterDuplicateUsernameThrowsException() {
        RegisterRequest request = new RegisterRequest("new@example.com", "existinguser", "Password123!", "Name");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        assertEquals("Username is already taken.", exception.getMessage());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");

        User user = new User("username", "user@example.com", "hashed", "Name");
        user.setId(5L);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user@example.com", 5L)).thenReturn("valid.jwt.token");
        when(jwtService.getJwtExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("valid.jwt.token", response.getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLoginBadCredentialsThrowsException() {
        LoginRequest request = new LoginRequest("user@example.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testGetCurrentUser() {
        User user = new User("myuser", "myuser@example.com", "secretHash", "My Display Name");
        user.setId(15L);

        when(userRepository.findByEmail("myuser@example.com")).thenReturn(Optional.of(user));

        UserProfileDto profile = authService.getCurrentUser("myuser@example.com");

        assertNotNull(profile);
        assertEquals(15L, profile.getId());
        assertEquals("myuser@example.com", profile.getEmail());
        assertEquals("myuser", profile.getUsername());
        assertEquals("My Display Name", profile.getDisplayName());
    }
}
