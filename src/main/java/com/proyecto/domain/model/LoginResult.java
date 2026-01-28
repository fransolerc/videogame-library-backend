package com.proyecto.domain.model;

public record LoginResult(String token, User user, String username) {
}
