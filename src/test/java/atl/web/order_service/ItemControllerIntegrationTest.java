package atl.web.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;

import atl.web.order_service.dto.ItemDto;
import atl.web.order_service.dto.ItemResponseDto;
import atl.web.order_service.repositories.ItemRepository;
import atl.web.order_service.client.AuthServiceClient;
import atl.web.order_service.dto.ValidateTokenRequestDto;
import atl.web.order_service.kafka.consumer.MessageConsumer;
import atl.web.order_service.kafka.producer.MessageProducer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @MockitoBean
    private AuthServiceClient authServiceClient;


    @MockitoBean
    private MessageConsumer messageConsumer;

    @MockitoBean
    private MessageProducer messageProducer;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

        registry.add("spring.liquibase.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.cache.type", () -> "none");
        registry.add("eureka.client.enabled", () -> "false");
    }

    @AfterAll
    static void closeContainer() {
        postgreSQLContainer.close();
    }

    @AfterEach
    void clearDb() {
        itemRepository.deleteAll();
    }

    private void setupMockAuth(String role) {
        when(authServiceClient.validateToken(any(ValidateTokenRequestDto.class))).thenReturn(true);
        when(authServiceClient.exrtactEmail(any(ValidateTokenRequestDto.class))).thenReturn("testuser");
        when(authServiceClient.extractRole(any(ValidateTokenRequestDto.class))).thenReturn(role);
    }

    private String getAuthHeader() {
        return "Bearer token";
    }

    private Long createTestItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .price(99.99)
                .build();

        setupMockAuth("ROLE_ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/v1/items")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ItemResponseDto createdItem = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ItemResponseDto.class);
        return createdItem.getId();
    }

    @Test
    @DisplayName("Should create and return item")
    void createItem_ShouldCreateAndReturnItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .price(99.99)
                .build();

        setupMockAuth("ROLE_ADMIN");

        MvcResult result = mockMvc.perform(post("/api/v1/items")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ItemResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ItemResponseDto.class);

        assertNotNull(response);
        assertEquals("Test Item", response.getName());
        assertEquals(99.99, response.getPrice());
    }

    @Test
    @DisplayName("Should return item if it exists")
    void getItemById_ShouldReturnItem_WhenItemExists() throws Exception {
        Long itemId = createTestItem();

        setupMockAuth("ROLE_USER");

        MvcResult getResult = mockMvc.perform(get("/api/v1/items/{id}", itemId)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ItemResponseDto response = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                ItemResponseDto.class);

        assertEquals(itemId, response.getId());
        assertEquals("Test Item", response.getName());
        assertEquals(99.99, response.getPrice());
    }

    @Test
    @DisplayName("Should return errorResponse if item not exists")
    void getItemById_ShouldReturnError_WhenItemNotFound() throws Exception {
        setupMockAuth("ROLE_USER");

        mockMvc.perform(get("/api/v1/items/{id}", 999L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return item by name")
    void getItemByName_ShouldReturnItem_WhenNameExists() throws Exception {
        createTestItem();
        setupMockAuth("ROLE_USER");

        MvcResult result = mockMvc.perform(get("/api/v1/items")
                .header("Authorization", getAuthHeader())
                .param("name", "Test Item"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        ItemResponseDto[] responses = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ItemResponseDto[].class);

        assertEquals(1, responses.length);
        assertEquals("Test Item", responses[0].getName());
    }

    @Test
    @DisplayName("Should update item if it exists")
    void updateItem_ShouldUpdateAndReturnItem() throws Exception {
        Long itemId = createTestItem();

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Item")
                .price(75.0)
                .build();

        setupMockAuth("ROLE_ADMIN");

        MvcResult updateResult = mockMvc.perform(put("/api/v1/items/{id}", itemId)
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andReturn();

        ItemResponseDto response = objectMapper.readValue(
                updateResult.getResponse().getContentAsString(),
                ItemResponseDto.class);

        assertEquals(itemId, response.getId());
        assertEquals("Updated Item", response.getName());
        assertEquals(75.0, response.getPrice());
    }

    @Test
    @DisplayName("Should delete item if it exists")
    void deleteItem_ShouldDeleteItem() throws Exception {
        Long itemId = createTestItem();

        setupMockAuth("ROLE_ADMIN");

        mockMvc.perform(delete("/api/v1/items/{id}", itemId)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());

        setupMockAuth("ROLE_USER");

        mockMvc.perform(get("/api/v1/items/{id}", itemId)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return forbidden when user without ADMIN role tries to create item")
    void createItem_ShouldReturnForbidden_WhenUserNotAdmin() throws Exception {
        setupMockAuth("ROLE_USER");
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .price(99.99)
                .build();


        mockMvc.perform(post("/api/v1/items")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return forbidden when user without tries to update item")
    void updateItem_ShouldReturnForbidden_WhenUserNotAdmin() throws Exception {
        setupMockAuth("ROLE_USER");
        ItemDto itemDto = ItemDto.builder()
                .name("Updated Item")
                .price(75.0)
                .build();

        mockMvc.perform(put("/api/v1/items/{id}", 1L)
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return forbidden when user tries to delete item")
    void deleteItem_ShouldReturnForbidden_WhenUserNotAdmin() throws Exception {
        setupMockAuth("ROLE_USER"); 

        mockMvc.perform(delete("/api/v1/items/{id}", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isForbidden());
    }

}