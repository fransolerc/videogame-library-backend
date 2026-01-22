package com.proyecto.infrastructure.adapter.out.persistence.repository;

import com.proyecto.infrastructure.adapter.out.persistence.entity.UserGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataUserGameRepository extends JpaRepository<UserGameEntity, String> {
    Optional<UserGameEntity> findByUser_IdAndGameId(String userId, String gameId);
    List<UserGameEntity> findByUser_Id(String userId);
}
