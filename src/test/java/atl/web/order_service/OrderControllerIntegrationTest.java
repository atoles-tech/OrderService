package atl.web.order_service;

import com.fasterxml.jackson.databind.ObjectMapper;

import atl.web.order_service.dto.*;
import atl.web.order_service.model.Status;
import atl.web.order_service.model.Item;
import atl.web.order_service.repositories.OrderRepository;
import atl.web.order_service.repositories.ItemRepository;
import atl.web.order_service.repositories.OrderItemRepository;
import atl.web.order_service.client.AuthServiceClient;
import atl.web.order_service.client.UserServiceClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private AuthServiceClient authServiceClient;

    @MockitoBean
    private UserServiceClient userServiceClient;

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

    private Long item1Id;
    private Long item2Id;

    @BeforeEach
    void setUp() {
        Item item1 = new Item();
        item1.setName("Test Item 1");
        item1.setPrice(10.0);
        item1 = itemRepository.save(item1);
        item1Id = item1.getId();

        Item item2 = new Item();
        item2.setName("Test Item 2");
        item2.setPrice(20.0);
        item2 = itemRepository.save(item2);
        item2Id = item2.getId();
    }

    @AfterAll
    static void closeContainer() {
        postgreSQLContainer.close();
    }

    @AfterEach
    void clearDb() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    private void setupMockAuth(String role, String username) {
        when(authServiceClient.validateToken(any(ValidateTokenRequestDto.class))).thenReturn(true);
        when(authServiceClient.exrtactEmail(any(ValidateTokenRequestDto.class))).thenReturn(username);
        when(authServiceClient.extractRole(any(ValidateTokenRequestDto.class))).thenReturn(role);
    }

    private void setupMockUserService(String email) {
        when(userServiceClient.getUserByEmail(email)).thenReturn(
                UserInfoDto.builder()
                        .id(123L)
                        .name("Test")
                        .surname("User")
                        .email(email)
                        .build());

        when(userServiceClient.getUser(123L)).thenReturn(
                UserInfoDto.builder()
                        .id(123L)
                        .name("Test")
                        .surname("User")
                        .email(email)
                        .build());
    }

    private String getAuthHeader() {
        return "Bearer token";
    }

    private OrderDto createTestOrderDto() {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderItems(Arrays.asList(
                OrderItemDto.builder().itemId(item1Id).quantity(2).build(),
                OrderItemDto.builder().itemId(item2Id).quantity(1).build()));
        return orderDto;
    }

    private Long createTestOrder() throws Exception {
        OrderDto orderDto = createTestOrderDto();
        setupMockAuth("ROLE_USER", "email@gmail.com");
        setupMockUserService("email@gmail.com");

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        OrderResponseWithUserDto createdOrder = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                OrderResponseWithUserDto.class);
        return createdOrder.getId();
    }

    @Test
    @DisplayName("Should create and return order")
    void createOrder_ShouldCreateAndReturnOrder() throws Exception {
        OrderDto orderDto = createTestOrderDto();
        setupMockAuth("ROLE_USER", "email@gmail.com");
        setupMockUserService("email@gmail.com");

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        OrderResponseWithUserDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                OrderResponseWithUserDto.class);

        assertNotNull(response);
        assertEquals(123L, response.getUserId());
        assertEquals(Status.PROCESSING, response.getStatus());
        assertNotNull(response.getOrderItems());
        assertEquals(2, response.getOrderItems().size());
    }

    @Test
    @DisplayName("Should return order if it exists")
    void getOrderById_ShouldReturnOrder_WhenOrderExists() throws Exception {
        Long orderId = createTestOrder();
        setupMockAuth("ROLE_USER", "email@gmail.com");
        setupMockUserService("email@gmail.com");

        MvcResult getResult = mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        OrderResponseWithUserDto response = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                OrderResponseWithUserDto.class);

        assertEquals(orderId, response.getId());
        assertEquals(123L, response.getUserId());
        assertEquals(Status.PROCESSING, response.getStatus());

        assertNotNull(response.getOrderItems());
        assertEquals(2, response.getOrderItems().size());

        List<OrderItemResponseDto> orderItems = response.getOrderItems();
        assertTrue(orderItems.stream().anyMatch(item -> item.getItem().getId().equals(item1Id)));
        assertTrue(orderItems.stream().anyMatch(item -> item.getItem().getId().equals(item2Id)));
    }

    @Test
    @DisplayName("Should return error if order not exists")
    void shouldReturnError_WhenOrderNotFound() throws Exception {
        setupMockAuth("ROLE_USER", "email@gmail.com");

        mockMvc.perform(get("/api/v1/orders/{id}", 999L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update order items if it exists")
    void shouldUpdateAndReturnOrder() throws Exception {
        Long orderId = createTestOrder();
        setupMockAuth("ROLE_USER", "email@gmail.com");
        setupMockUserService("email@gmail.com");

        OrderDto updateDto = new OrderDto();
        updateDto.setOrderItems(Arrays.asList(
                OrderItemDto.builder().itemId(item1Id).quantity(5).build(),
                OrderItemDto.builder().itemId(item2Id).quantity(3).build()));
        MvcResult updateResult = mockMvc.perform(put("/api/v1/orders/{id}/items", orderId)
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseWithUserDto response = objectMapper.readValue(
                updateResult.getResponse().getContentAsString(),
                OrderResponseWithUserDto.class);

        assertEquals(orderId, response.getId());
        assertEquals(123L, response.getUserId());
        List<OrderItemResponseDto> orderItems = response.getOrderItems();
        OrderItemResponseDto item1 = orderItems.stream()
                .filter(item -> item.getItem().getId().equals(item1Id))
                .findFirst()
                .orElseThrow();
        assertEquals(5, item1.getQuantity());

        OrderItemResponseDto item2 = orderItems.stream()
                .filter(item -> item.getItem().getId().equals(item2Id))
                .findFirst()
                .orElseThrow();
        assertEquals(3, item2.getQuantity());
    }

    @Test
    @DisplayName("Should update order status if it exists")
    void shouldUpdateAndReturnOrder_WhenItExists() throws Exception {
        Long orderId = createTestOrder();
        setupMockAuth("ROLE_USER", "email@gmail.com");
        setupMockUserService("email@gmail.com");

        MvcResult updateResult = mockMvc.perform(put("/api/v1/orders/{id}/status?status=DELIVERED", orderId)
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        OrderResponseWithUserDto response = objectMapper.readValue(
                updateResult.getResponse().getContentAsString(),
                OrderResponseWithUserDto.class);

        assertEquals(orderId, response.getId());
        assertEquals(Status.DELIVERED, response.getStatus());
    }

    @Test
    @DisplayName("Should delete order if it exists")
    void shouldDeleteOrder() throws Exception {
        Long orderId = createTestOrder();
        setupMockAuth("ROLE_ADMIN", "admin");

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());

        setupMockAuth("ROLE_USER", "email@gmail.com");

        mockMvc.perform(get("/api/v1/orders/{id}", orderId)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return forbidden when user without ADMIN role tries to get all orders")
    void getAllOrders_ShouldReturnForbidden_WhenUserNotAdmin() throws Exception {
        setupMockAuth("ROLE_USER", "email@gmail.com");

        mockMvc.perform(get("/api/v1/orders")
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return forbidden when user tries to access other user orders")
    void getUserOrders_ShouldReturnForbidden() throws Exception {
        setupMockAuth("ROLE_USER", "email@gmail.com");

        mockMvc.perform(get("/api/v1/users/{userId}/orders", 456L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return forbidden when user without ADMIN role tries to delete order")
    void deleteOrder_ShouldReturnForbidden_WhenUserNotAdmin() throws Exception {
        setupMockAuth("ROLE_USER", "email@gmail.com");

        mockMvc.perform(delete("/api/v1/orders/{id}", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return error when item not found")
    void createOrder_ShouldReturnError_WhenItemNotFound() throws Exception {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderItems(Arrays.asList(
                OrderItemDto.builder().itemId(999L).quantity(2).build()));
        setupMockAuth("ROLE_USER", "email@gmail.com");

        when(userServiceClient.getUserByEmail("email@gmail.com")).thenReturn(new UserInfoDto(1L, null, null, null, null));

        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isNotFound());
    }
}