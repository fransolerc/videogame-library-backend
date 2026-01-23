package com.proyecto.infrastructure.adapter.out.persistence;

import com.proyecto.application.port.out.LibraryRepositoryPort;
import com.proyecto.domain.model.UserGame;
import com.proyecto.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.proyecto.infrastructure.adapter.out.persistence.entity.UserGameEntity;
import com.proyecto.infrastructure.adapter.out.persistence.repository.SpringDataUserGameRepository;
import com.proyecto.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaLibraryAdapter implements LibraryRepositoryPort {

    private final SpringDataUserGameRepository userGameRepository;
    private final SpringDataUserRepository userRepository;

    public JpaLibraryAdapter(SpringDataUserGameRepository userGameRepository, SpringDataUserRepository userRepository) {
        this.userGameRepository = userGameRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserGame save(UserGame userGame) {
        UserEntity userEntity = userRepository.findById(userGame.userId())
                .orElseThrow(() -> new RuntimeException("User not found")); // O una excepción más específica

        UserGameEntity entity = toEntity(userGame, userEntity);
        UserGameEntity savedEntity = userGameRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<UserGame> findByUserIdAndGameId(String userId, Long gameId) {
        return userGameRepository.findByUser_IdAndGameId(userId, gameId).map(this::toDomain);
    }

    @Override
    public List<UserGame> findByUserId(String userId) {
        return userGameRepository.findByUser_Id(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    private UserGameEntity toEntity(UserGame userGame, UserEntity userEntity) {
        return new UserGameEntity(userEntity, userGame.gameId(), userGame.status());
    }

    private UserGame toDomain(UserGameEntity entity) {
        return new UserGame(entity.getUser().getId(), entity.getGameId(), entity.getStatus(), entity.getAddedAt());
    }
}
