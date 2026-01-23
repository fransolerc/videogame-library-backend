package com.proyecto.application.service;

import com.proyecto.application.port.in.LoginUserUseCase;
import com.proyecto.application.port.in.RegisterUserUseCase;
import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.exception.EmailAlreadyExistsException;
import com.proyecto.domain.model.User;
import com.proyecto.infrastructure.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements RegisterUserUseCase, LoginUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public User registerUser(String username, String email, String password) {
        userRepositoryPort.findByEmail(email).ifPresent(_ -> {
            throw new EmailAlreadyExistsException("El email '" + email + "' ya está registrado.");
        });

        String encodedPassword = passwordEncoder.encode(password);

        User newUser = new User(UUID.randomUUID().toString(), username, email, encodedPassword);

        return userRepositoryPort.save(newUser);
    }

    @Override
    public Optional<String> loginUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtTokenProvider.generateToken(authentication);
            return Optional.of(jwt);
        } catch (Exception _) {
            return Optional.empty();
        }
    }
}