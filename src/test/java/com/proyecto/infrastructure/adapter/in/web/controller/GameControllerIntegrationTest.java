package com.proyecto.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.videogames.generated.model.GameFilterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class GameControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "Zelda", "Final Fantasy"})
    void shouldReturn200WhenSearchingGames(String query) throws Exception {
        mockMvc.perform(get("/games/search").param("name", query))
               .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(longs = {99999999L, 99999998L})
    void shouldReturn404WhenGameDoesNotExist(long id) throws Exception {
        mockMvc.perform(get("/games/{id}", id))
               .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("provideGameFilters")
    void shouldReturn200WhenFilteringGames(GameFilterRequestDTO filterRequest) throws Exception {
        mockMvc.perform(post("/games/filter")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(filterRequest)))
               .andExpect(status().isOk());
    }

    private static Stream<Arguments> provideGameFilters() {
        GameFilterRequestDTO filter1 = new GameFilterRequestDTO();
        filter1.setFilter("genres = (12, 32)");

        GameFilterRequestDTO filter2 = new GameFilterRequestDTO();
        filter2.setFilter("platforms = (6, 48) & rating > 80");
        filter2.setSort("rating desc");

        return Stream.of(
                Arguments.of(filter1),
                Arguments.of(filter2)
        );
    }

    @ParameterizedTest
    @MethodSource("provideGameIds")
    void shouldReturn200WhenGettingGamesByIds(List<Long> ids) throws Exception {
        mockMvc.perform(post("/games/batch")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(ids)))
               .andExpect(status().isOk());
    }

    private static Stream<Arguments> provideGameIds() {
        return Stream.of(
                Arguments.of(List.of(1L, 2L, 3L)),
                Arguments.of(Collections.emptyList()),
                Arguments.of(List.of(101L))
        );
    }
}
