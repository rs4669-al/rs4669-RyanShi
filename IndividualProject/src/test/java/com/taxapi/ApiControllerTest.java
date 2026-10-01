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
        // Placeholder retained; real coverage comes from tests below.
    }

    // ---------- POST /v1/clients ----------

    @Test
    void createClient_newName_returnsOk() throws Exception {
        String body = "{\"name\":\"Charlie\"}";
        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Charlie"));
    }

    @Test
    void createClient_existingName_returnsConflict() throws Exception {
        String body = "{\"name\":\"Alice\"}";
        mockMvc.perform(post("/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict());
    }

    // ---------- POST /v1/items ----------

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
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mouse"));
    }

    // ---------- GET /v1/items ----------

    @Test
    void getItems_invalidApiKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", INVALID_KEY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getItems_validApiKey_returnsOk() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("item-1"));
    }

    @Test
    void getItems_categoryFilter_returnsMatchingItems() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .param("category", "electronics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getItems_queryFilter_returnsMatchingItems() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .param("q", "top"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    void getItems_nonMatchingFilters_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .param("category", "clothing")
                .param("q", "phone"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getItems_matchingCategoryButNonMatchingQuery_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/v1/items")
                .header("X-API-Key", VALID_KEY)
                .param("category", "electronics")
                .param("q", "phone"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void updateItemPrice_invalidApiKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", INVALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"basePrice\":1099.99}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateItemPrice_found_returnsUpdatedItem() throws Exception {
        mockMvc.perform(patch("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"basePrice\":1099.99}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("item-1"))
            .andExpect(jsonPath("$.name").value("Laptop"))
            .andExpect(jsonPath("$.category").value("electronics"))
            .andExpect(jsonPath("$.basePrice").value(1099.99));
    }

    @Test
    void updateItemPrice_notFound_returns404() throws Exception {
        mockMvc.perform(patch("/v1/items/no-such-id")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"basePrice\":10.00}"))
            .andExpect(status().isNotFound());
    }

    // ---------- GET /v1/items/{id} ----------

    @Test
    void getItemById_invalidApiKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/items/item-1")
                .header("X-API-Key", INVALID_KEY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getItemById_found_returnsOk() throws Exception {
        mockMvc.perform(get("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getItemById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/v1/items/no-such-id")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNotFound());
    }

    // ---------- DELETE /v1/items/{id} ----------

    @Test
    void deleteItem_invalidApiKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/v1/items/item-1")
                .header("X-API-Key", INVALID_KEY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteItem_found_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/v1/items/item-1")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteItem_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/v1/items/no-such-id")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNotFound());
    }

    // ---------- POST /v1/tax/quote ----------

    @Test
    void calculateTax_invalidApiKey_returnsUnauthorized() throws Exception {
        String body = "{\"itemId\":\"item-1\",\"state\":\"CA\"}";
        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", INVALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void calculateTax_validItemAndSupportedState_returnsOk() throws Exception {
        // item-1 is electronics; CA supports electronics per taxrates.json
        String body = "{\"itemId\":\"item-1\",\"state\":\"CA\"}";
        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.taxAmount").value(72.499275))
            .andExpect(jsonPath("$.total").value(1072.489275));
    }

    @Test
    void calculateTax_unsupportedStateOrCategory_returnsBadRequest() throws Exception {
        // NY only supports "clothing" per taxrates.json; item-1 is electronics
        String body = "{\"itemId\":\"item-1\",\"state\":\"NY\"}";
        mockMvc.perform(post("/v1/tax/quote")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    // ---------- GET /v1/supported ----------

    @Test
    void getSupported_invalidApiKey_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/supported")
                .header("X-API-Key", INVALID_KEY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getSupported_validApiKey_returnsOk() throws Exception {
        mockMvc.perform(get("/v1/supported")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.states", org.hamcrest.Matchers.hasItem("CA")));
    }
}