package com.proyecto.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.application.port.out.UserRepositoryPort;
import com.proyecto.domain.model.User;
import com.proyecto.infrastructure.security.jwt.JwtTokenProvider;
import com.proyecto.videogames.generated.model.GameStatusDTO;
import com.proyecto.videogames.generated.model.UpdateGameStatusRequestDTO;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LibraryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String jwtToken;
    private String userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        User testUser = new User(UUID.randomUUID().toString(), "testuser", "test@example.com", passwordEncoder.encode("password"));
        userRepositoryPort.save(testUser);
        userId = testUser.id();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(testUser.email())
                .password(testUser.password())
                .authorities(new ArrayList<>())
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        jwtToken = jwtTokenProvider.generateToken(authentication);
    }

    @Test
    void shouldReturnEmptyListWhenListingNewUserLibrary() throws Exception {
        mockMvc.perform(get("/users/{userId}/games", userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturn200WhenUpsertingGame() throws Exception {
        UpdateGameStatusRequestDTO request = new UpdateGameStatusRequestDTO().status(GameStatusDTO.PLAYING);

        mockMvc.perform(put("/users/{userId}/games/{gameId}", userId, 1L)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenAddingFavorite() throws Exception {
        mockMvc.perform(post("/users/{userId}/games/{gameId}/favorite", userId, 1L)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite", is(true)));
    }

    @Test
    void shouldReturnEmptyPageWhenListingFavoritesForNewUser() throws Exception {
        mockMvc.perform(get("/users/{userId}/favorites", userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldReturn404WhenGameStatusIsForNonExistentGame() throws Exception {
        mockMvc.perform(get("/users/{userId}/games/{gameId}", userId, 99999999L)
                        .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn204WhenRemovingNonExistentGame() throws Exception {
        mockMvc.perform(delete("/users/{userId}/games/{gameId}", userId, 99999999L)
                        .header("Authorization", "Bearer " + jwtToken))
               .andExpect(status().isNoContent());
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentFavorite() {
        Exception exception = assertThrows(ServletException.class, () -> mockMvc.perform(delete("/users/{userId}/games/{gameId}/favorite", userId, 99999999L)
                        .header("Authorization", "Bearer " + jwtToken)));

        Throwable rootCause = exception.getCause();
        assertInstanceOf(RuntimeException.class, rootCause);
        assertTrue(rootCause.getMessage().contains("Game not found in library"));
    }
}
