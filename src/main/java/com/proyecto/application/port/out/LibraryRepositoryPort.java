package com.proyecto.application.port.out;

import com.proyecto.domain.model.UserGame;
import java.util.List;
import java.util.Optional;

public interface LibraryRepositoryPort {
    UserGame save(UserGame userGame);
    Optional<UserGame> findByUserIdAndGameId(String userId, Long gameId);
    List<UserGame> findByUserId(String userId);
    UserGame update(UserGame userGame);
}
