package com.proyecto.application.port.out;

import com.proyecto.domain.model.UserGame;
import java.util.List;
import java.util.Optional;

public interface LibraryRepositoryPort {
    UserGame save(UserGame userGame);
    Optional<UserGame> findByUserIdAndGameId(String userId, String gameId);
    List<UserGame> findByUserId(String userId);
}
