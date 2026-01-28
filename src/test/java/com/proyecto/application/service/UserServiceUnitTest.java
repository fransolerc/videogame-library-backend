package com.proyecto.application.service;

import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.exception.EmailAlreadyExistsException;
import com.proyecto.domain.model.LoginResult;
import com.proyecto.domain.model.User;
import com.proyecto.infrastructure.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private final String username = "testuser";
    private final String email = "test@example.com";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword";

    @Test
    void registerUser_ShouldSaveNewUser_WhenEmailIsUnique() {
        // Arrange
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        
        User savedUser = new User(UUID.randomUUID().toString(), username, email, encodedPassword);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(username, email, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.username());
        assertEquals(email, result.email());
        assertEquals(encodedPassword, result.password());

        verify(userRepositoryPort).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        User existingUser = new User(UUID.randomUUID().toString(), "existing", email, "pass");
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> 
            userService.registerUser(username, email, password)
        );

        verify(userRepositoryPort).findByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void loginUser_ShouldReturnToken_WhenCredentialsAreValid() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        
        String expectedToken = "jwt-token";
        when(jwtTokenProvider.generateToken(authentication)).thenReturn(expectedToken);

        User user = new User(UUID.randomUUID().toString(), username, email, encodedPassword);
        when(userRepositoryPort.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional<LoginResult> result = userService.loginUser(email, password);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedToken, result.get().token());
        assertEquals(username, result.get().username());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    void loginUser_ShouldReturnEmpty_WhenAuthenticationFails() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // Act
        Optional<LoginResult> result = userService.loginUser(email, password);

        // Assert
        assertTrue(result.isEmpty());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, never()).generateToken(any());
    }
}
