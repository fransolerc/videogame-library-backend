package com.proyecto.infrastructure.adapter.out.persistence.repository;

import com.proyecto.infrastructure.adapter.out.persistence.entity.UserGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataUserGameRepository extends JpaRepository<UserGameEntity, String> {
    Optional<UserGameEntity> findByUserIdAndGameId(String userId, Long gameId);
    List<UserGameEntity> findByUserId(String userId);
    void deleteByUserIdAndGameId(String userId, Long gameId);
}
