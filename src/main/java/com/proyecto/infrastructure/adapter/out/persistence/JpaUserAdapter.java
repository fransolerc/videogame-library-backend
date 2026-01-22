package com.proyecto.infrastructure.adapter.out.persistence;

import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.model.User;
import com.proyecto.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.proyecto.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaUserAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository userRepository;

    public JpaUserAdapter(SpringDataUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity savedEntity = userRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(user.id(), user.username(), user.email(), user.password());
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getEmail(), entity.getPassword());
    }
}
