package com.taxapi;

import com.taxapi.service.TaxApiService;
import com.taxapi.model.Client;
import com.taxapi.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
class TaxApiServiceUnitTests {

    @Autowired
    private TaxApiService service;

    @Autowired
    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
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
    void createClient_existingName_createsClient() throws Exception {
        Client created = service.createClient("Alice");
        assertNotNull(created);
    }

    @Test
    void validateApiKey_validKey_returnsTrue() throws Exception {
        assertTrue(service.validateApiKey("valid-key"));
    }

    @Test
    void validateApiKey_nullKey_returnsFalse() throws Exception {
        assertFalse(service.validateApiKey(null));
    }

    @Test
    void createItem_addsNewItem() throws Exception {
        Item created = service.createItem("Desk Lamp", "electronics", 49.99);
        assertNotNull(created);
        assertEquals("Desk Lamp", created.getName());
    }

    @Test
    void getItemById_found() throws Exception {
        Item item = service.getItemById("item-1");
        assertNotNull(item);
    }
}