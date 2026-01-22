package com.proyecto.infrastructure.adapter.out.persistence.entity;

import com.proyecto.domain.model.GameStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_games")
public class UserGameEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "game_id", nullable = false)
    private String gameId; // ID externo de IGDB

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    public UserGameEntity() {
        this.id = UUID.randomUUID().toString();
        this.addedAt = LocalDateTime.now();
    }

    public UserGameEntity(UserEntity user, String gameId, GameStatus status) {
        this();
        this.user = user;
        this.gameId = gameId;
        this.status = status;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
