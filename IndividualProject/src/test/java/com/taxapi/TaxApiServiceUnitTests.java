package com.taxapi;

import com.taxapi.service.TaxApiService;
import com.taxapi.model.Client;
import com.taxapi.model.Item;
import com.taxapi.model.TaxQuoteRequest;
import com.taxapi.model.TaxQuoteResponse;
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
    void createClient_newName_createsClient() throws Exception {
        Client created = service.createClient("Charlie");
        assertNotNull(created);
        assertEquals("Charlie", created.getName());
    }

    @Test
    void createClient_existingName_returnsNull() throws Exception {
        Client created = service.createClient("Alice");
        assertNull(created);
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
    void validateApiKey_invalidKey_returnsFalse() throws Exception {
        assertFalse(service.validateApiKey("wrong-key"));
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

    @Test
    void getItemById_missing_returnsNull() throws Exception {
        Item item = service.getItemById("missing");
        assertNull(item);
    }

    @Test
    void updateItemPrice_existingId_updatesAndReturnsItem()
        throws Exception {
        Item updated =
            service.updateItemPrice("item-1", 1099.99);

        assertNotNull(updated);
        assertEquals(1099.99, updated.getBasePrice());
    }

    @Test
    void updateItemPrice_nonExistentId_returnsNull()
        throws Exception {
        Item updated =
            service.updateItemPrice(
                "no-such-id",
                10.00
            );

        assertNull(updated);
    }

    @Test
    void calculateTax_withItemId_returnsCorrectAmounts() throws Exception {
        TaxQuoteRequest request = new TaxQuoteRequest();
        request.setItemId("item-1");
        request.setState("CA");

        TaxQuoteResponse response = service.calculateTax(request);

        assertNotNull(response);
        assertEquals(999.99, response.getPrice(), 0.001);
        assertEquals(0.0725, response.getTaxRate(), 0.000001);
        assertEquals(72.499275, response.getTaxAmount(), 0.001);
        assertEquals(1072.489275, response.getTotal(), 0.001);
    }

    @Test
    void calculateTax_withDirectPrice_returnsCorrectAmounts() throws Exception {
        TaxQuoteRequest request = new TaxQuoteRequest();
        request.setPrice(100.00);
        request.setCategory("clothing");
        request.setState("NY");

        TaxQuoteResponse response = service.calculateTax(request);

        assertNotNull(response);
        assertEquals(100.00, response.getPrice(), 0.001);
        assertEquals(0.04, response.getTaxRate(), 0.000001);
        assertEquals(4.00, response.getTaxAmount(), 0.001);
        assertEquals(104.00, response.getTotal(), 0.001);
    }

    @Test
    void calculateTax_missingItem_returnsNull() throws Exception {
        TaxQuoteRequest request = new TaxQuoteRequest();
        request.setItemId("missing");
        request.setState("CA");

        assertNull(service.calculateTax(request));
    }

    @Test
    void calculateTax_unsupportedCategory_returnsNull() throws Exception {
        TaxQuoteRequest request = new TaxQuoteRequest();
        request.setPrice(100.00);
        request.setCategory("unknown");
        request.setState("CA");

        assertNull(service.calculateTax(request));
    }
}