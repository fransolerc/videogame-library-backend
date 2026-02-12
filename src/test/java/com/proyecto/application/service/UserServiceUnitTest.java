package com.proyecto.application.service;

import com.proyecto.application.port.out.persistence.UserRepositoryInterface;
import com.proyecto.domain.exception.EmailAlreadyExistsException;
import com.proyecto.domain.model.LoginResult;
import com.proyecto.domain.model.User;
import com.proyecto.infrastructure.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepositoryInterface userRepositoryInterface;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceService userService;

    private final String username = "testuser";
    private final String email = "test@example.com";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword";

    @ParameterizedTest
    @MethodSource("provideRegisterUserArguments")
    void registerUser_ShouldHandleAllCases(User existingUser, Class<? extends Exception> expectedException) {
        // Arrange
        when(userRepositoryInterface.findByEmail(email)).thenReturn(Optional.ofNullable(existingUser));
        if (existingUser == null) {
            when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
            User savedUser = new User(UUID.randomUUID().toString(), username, email, encodedPassword);
            when(userRepositoryInterface.save(any(User.class))).thenReturn(savedUser);
        }

        // Act & Assert
        if (expectedException != null) {
            assertThrows(expectedException, () -> userService.registerUser(username, email, password));
            verify(userRepositoryInterface, never()).save(any());
        } else {
            User result = userService.registerUser(username, email, password);
            assertNotNull(result);
            assertEquals(username, result.username());
            assertEquals(email, result.email());
            assertEquals(encodedPassword, result.password());
            verify(userRepositoryInterface).save(any(User.class));
        }

        verify(userRepositoryInterface).findByEmail(email);
    }

    private static Stream<Arguments> provideRegisterUserArguments() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(mock(User.class), EmailAlreadyExistsException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideLoginUserArguments")
    void loginUser_ShouldReturnTokenOrEmpty(boolean validCredentials, LoginResult expectedResult) {
        // Arrange
        if (validCredentials) {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            String expectedToken = "jwt-token";
            when(jwtTokenProvider.generateToken(authentication)).thenReturn(expectedToken);
            User user = new User(UUID.randomUUID().toString(), username, email, encodedPassword);
            when(userRepositoryInterface.findByEmail(email)).thenReturn(Optional.of(user));
        } else {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new RuntimeException("Bad credentials"));
        }

        // Act
        Optional<LoginResult> result = userService.loginUser(email, password);

        // Assert
        assertEquals(expectedResult != null, result.isPresent());
        if (expectedResult != null) {
            assertTrue(result.isPresent());
            assertEquals(expectedResult.username(), result.get().username());
        }

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider, times(validCredentials ? 1 : 0)).generateToken(any());
    }

    private static Stream<Arguments> provideLoginUserArguments() {
        String username = "testuser";
        String token = "jwt-token";
        User user = new User(UUID.randomUUID().toString(), username, "test@example.com", "encodedPassword");
        LoginResult loginResult = new LoginResult(token, user, username);
        return Stream.of(
                Arguments.of(true, loginResult),
                Arguments.of(false, null)
        );
    }
}
