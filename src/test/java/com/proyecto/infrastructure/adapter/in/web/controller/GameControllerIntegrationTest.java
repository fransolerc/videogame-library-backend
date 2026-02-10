package com.proyecto.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.videogames.generated.model.GameFilterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

    @Test
    void shouldReturn200WhenSearchingGames() throws Exception {
        // This is now an end-to-end test, it will call the real use case.
        mockMvc.perform(get("/games/search").param("name", "test"))
               .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenGameDoesNotExist() throws Exception {
        // A non-existent ID should return 404
        mockMvc.perform(get("/games/{id}", 99999999L))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200WhenFilteringGames() throws Exception {
        GameFilterRequestDTO filterRequest = new GameFilterRequestDTO();
        filterRequest.setFilter("genres = (12, 32)");

        // This is now an end-to-end test
        mockMvc.perform(post("/games/filter")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(filterRequest)))
               .andExpect(status().isOk());
    }
}
