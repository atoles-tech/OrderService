package atl.web.order_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import atl.web.order_service.client.UserServiceClient;
import atl.web.order_service.dto.*;
import atl.web.order_service.exceptions.OrderNotFoundException;
import atl.web.order_service.exceptions.RepeatbleItemException;
import atl.web.order_service.exceptions.UserNotFoundException;
import atl.web.order_service.kafka.producer.MessageProducer;
import atl.web.order_service.mappers.OrderMapper;
import atl.web.order_service.model.Item;
import atl.web.order_service.model.Order;
import atl.web.order_service.model.Status;
import atl.web.order_service.repositories.OrderRepository;
import atl.web.order_service.services.ItemService;
import atl.web.order_service.services.OrderService;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemService itemService;
    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderResponseWithUserDto orderResponseWithUserDto;
    private UserInfoDto userInfoDto;
    private OrderDto orderDto;

    @BeforeEach
    public void init(){
        order = new Order(1L, 1L, Status.PROCESSING, new Date(), Arrays.asList());
        
        userInfoDto = UserInfoDto.builder()
                .id(1L)
                .name("name")
                .surname("surname")
                .email("username@gmail.com")
                .build();

        orderResponseWithUserDto = OrderResponseWithUserDto.builder()
                .id(1L)
                .userId(1L)
                .status(Status.PROCESSING)
                .creationDate(new Date())
                .userInfo(userInfoDto)
                .build();

        orderDto = new OrderDto();
        orderDto.setOrderItems(Arrays.asList(
                OrderItemDto.builder().itemId(1L).quantity(2).build()
        ));
    }

    @Test
    @DisplayName("Should return order with user info when order exists")
    public void shouldReturnOrderWithUserInfo_WhenOrderExists(){
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponseWithUserDto(order)).thenReturn(orderResponseWithUserDto);
        when(userServiceClient.getUser(1L)).thenReturn(userInfoDto);

        OrderResponseWithUserDto response = orderService.getById(1L);

        assertEquals(orderResponseWithUserDto, response);
        assertEquals(userInfoDto, response.getUserInfo());
    }

    @Test
    @DisplayName("Should throw exception when order not found")
    public void shouldThrowException_WhenOrderNotFound(){
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getById(1L));
    }

    @Test
    @DisplayName("Should create and return order")
    public void shouldCreateAndReturnOrder(){
        List<Item> items = Arrays.asList(new Item(1L, "Item 1", 10.0));
        when(itemService.getItemByIds(Arrays.asList(1L))).thenReturn(items);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toOrderResponseWithUserDto(order)).thenReturn(orderResponseWithUserDto);
        when(userServiceClient.getUserByEmail("username@gmail.com")).thenReturn(userInfoDto);

        OrderResponseWithUserDto response = orderService.createOrder("username@gmail.com", orderDto);

        assertEquals(orderResponseWithUserDto, response);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when duplicate items in order")
    public void shouldThrowException_WhenDuplicateItemsInOrder(){
        orderDto.setOrderItems(Arrays.asList(
                OrderItemDto.builder().itemId(1L).quantity(2).build(),
                OrderItemDto.builder().itemId(1L).quantity(1).build()
        ));
        when(userServiceClient.getUserByEmail("username@gmail.com")).thenReturn(userInfoDto);

        assertThrows(RepeatbleItemException.class, () -> orderService.createOrder("username@gmail.com", orderDto));
    }

    @Test
    @DisplayName("Should return true when user is order owner")
    public void shouldReturnTrue_WhenUserIsOrderOwner(){
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userServiceClient.getUserByEmail("username@gmail.com")).thenReturn(userInfoDto);

        Boolean result = orderService.isOrderOwner(1L, "username@gmail.com");

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user is not order owner")
    public void shouldReturnFalse_WhenUserIsNotOrderOwner(){
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(UserNotFoundException.class, () -> orderService.isOrderOwner(1L, "usern123ame@gmail.com"));
    }

    @Test
    @DisplayName("Should throw exception when checking owner of non-existent order")
    public void shouldThrowException_WhenCheckingOwnerOfNonExistentOrder(){
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.isOrderOwner(1L, "username@gmail.com"));
    }

    @Test
    @DisplayName("Should return orders by status")
    public void shouldReturnOrdersByStatus(){
        List<Order> orders = Arrays.asList(order);
        List<OrderResponseDto> expectedResponse = Arrays.asList(
                OrderResponseDto.builder()
                        .id(1L)
                        .userId(1L)
                        .status(Status.PROCESSING)
                        .creationDate(new Date())
                        .build()
        );

        when(orderRepository.findByStatus(Status.PROCESSING)).thenReturn(orders);
        when(orderMapper.toOrderResponseList(orders)).thenReturn(expectedResponse);

        List<OrderResponseDto> response = orderService.getByStatus(Status.PROCESSING);

        assertEquals(1, response.size());
        assertEquals(expectedResponse.get(0), response.get(0));
    }

}