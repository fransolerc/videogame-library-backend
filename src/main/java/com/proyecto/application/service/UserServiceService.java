package com.proyecto.application.service;

import com.proyecto.application.port.in.UserServiceInterface;
import com.proyecto.application.port.out.persistence.UserRepositoryInterface;
import com.proyecto.domain.exception.EmailAlreadyExistsException;
import com.proyecto.domain.model.LoginResult;
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
public class UserServiceService implements UserServiceInterface {

    private final UserRepositoryInterface userRepositoryInterface;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceService(UserRepositoryInterface userRepositoryInterface, PasswordEncoder passwordEncoder,
                              AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepositoryInterface = userRepositoryInterface;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public User registerUser(String username, String email, String password) {
        userRepositoryInterface.findByEmail(email).ifPresent(_ -> {
            throw new EmailAlreadyExistsException("El email '" + email + "' ya está registrado.");
        });

        String encodedPassword = passwordEncoder.encode(password);
        User newUser = new User(UUID.randomUUID().toString(), username, email, encodedPassword);
        return userRepositoryInterface.save(newUser);
    }

    @Override
    public Optional<LoginResult> loginUser(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            User user = userRepositoryInterface.findByEmail(email).orElseThrow();

            return Optional.of(new LoginResult(jwt, user, user.username()));
        } catch (Exception _) {
            return Optional.empty();
        }
    }
}
