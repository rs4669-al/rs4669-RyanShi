package com.taxapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestConfig.class)
class ApiControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private LocalStorageService localStorageService;

    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    protected static final String VALID_KEY = "valid-key";
    protected static final String INVALID_KEY = "wrong-key";

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Files.writeString(tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"Alice\",\"apiKey\":\"valid-key\"}]");
        Files.writeString(tempDir.resolve("items.json"),
            "[{\"id\":\"item-1\",\"name\":\"Laptop\",\"category\":\"electronics\",\"basePrice\":999.99}]");
        Files.writeString(tempDir.resolve("taxrates.json"),
            "[{\"state\":\"CA\",\"category\":\"electronics\",\"rate\":0.0725},"
            + "{\"state\":\"NY\",\"category\":\"clothing\",\"rate\":0.04}]");

        localStorageService.setDirectory(tempDir);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createClient_existingName_returnsOk() throws Exception {
        String body = "{\"name\":\"Alice\"}";
        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }

    @Test
    void createClient_newName_returnsConflict() throws Exception {
        String body = "{\"name\":\"Charlie\"}";
        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict());
    }

    @Test
    void createItem_invalidApiKey_returnsUnauthorized() throws Exception {
        String body = "{\"name\":\"Mouse\",\"category\":\"electronics\",\"basePrice\":19.99}";
        mockMvc.perform(post("/v1/items")
                .header("X-API-Key", INVALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createItem_validApiKey_returnsOk() throws Exception {
        String body = "{\"name\":\"Mouse\",\"category\":\"electronics\",\"basePrice\":19.99}";
        mockMvc.perform(post("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }

    @Test
    void getItems_validApiKey_returnsOk() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk());
    }

    @Test
    void getItemById_found_returnsOk() throws Exception {
        mockMvc.perform(get("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk());
    }

    @Test
    void deleteItem_found_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNoContent());
    }

    @Test
    void calculateTax_validItemAndSupportedState_returnsOk() throws Exception {
        String body = "{\"itemId\":\"item-1\",\"state\":\"CA\"}";
        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }

    @Test
    void calculateTax_unsupportedState_returnsBadRequest() throws Exception {
        String body = "{\"itemId\":\"item-1\",\"state\":\"NY\"}";
        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSupported_validApiKey_returnsOk() throws Exception {
        mockMvc.perform(get("/v1/supported")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk());
    }
}