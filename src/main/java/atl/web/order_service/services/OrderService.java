package atl.web.order_service.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.order_service.client.UserServiceClient;
import atl.web.order_service.dto.OrderDto;
import atl.web.order_service.dto.OrderItemDto;
import atl.web.order_service.dto.OrderResponseDto;
import atl.web.order_service.dto.OrderResponseWithUserDto;
import atl.web.order_service.dto.UserInfoDto;
import atl.web.order_service.exceptions.ItemNotFoundException;
import atl.web.order_service.exceptions.OrderNotFoundException;
import atl.web.order_service.exceptions.RepeatbleItemException;
import atl.web.order_service.exceptions.StatusException;
import atl.web.order_service.exceptions.UserNotFoundException;
import atl.web.order_service.mappers.OrderMapper;
import atl.web.order_service.model.Item;
import atl.web.order_service.model.Order;
import atl.web.order_service.model.OrderItem;
import atl.web.order_service.model.Status;
import atl.web.order_service.repositories.OrderRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private UserServiceClient userServiceClient;

    private OrderMapper orderMapper;

    private OrderRepository orderRepository;

    private ItemService itemService;

    // read-admin
    public Page<OrderResponseDto> getAll(Pageable pageable) {
        return orderMapper.toOrderResponseDtoPage(
                orderRepository.findAll(pageable));
    }

    public List<OrderResponseDto> getAll() {
        return orderMapper.toOrderResponseList(
                orderRepository.findAll());
    }

    public Page<OrderResponseDto> getByStatus(Status status, Pageable pageable) {
        return orderMapper.toOrderResponseDtoPage(
                orderRepository.findByStatus(status, pageable));
    }

    public List<OrderResponseDto> getByStatus(Status status) {
        return orderMapper.toOrderResponseList(
                orderRepository.findByStatus(status));
    }

    // read-user
    public Page<OrderResponseWithUserDto> getAllByUserId(Long userId, Pageable pageable) {
        return orderMapper.toOrderResponseWithUserDtoPage(
                orderRepository.findByUserId(userId, pageable), userServiceClient.getUser(userId));
    }

    public OrderResponseWithUserDto getById(Long id) {
        OrderResponseWithUserDto order = orderMapper.toOrderResponseWithUserDto(orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id)));

        order.setUserInfo(userServiceClient.getUser(order.getUserId()));

        return order;
    }

    // update
    @Transactional
    public OrderResponseWithUserDto updateOrder(Long id, OrderDto orderDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setOrderItems(getOrderItemsByOrder(orderDto, order));

        OrderResponseWithUserDto response = orderMapper.toOrderResponseWithUserDto(order);
        response.setUserInfo(userServiceClient.getUser(order.getUserId()));
        return response;
    }

    @Transactional
    public OrderResponseWithUserDto updateOrderStatus(Long id, Status status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        Status currentStatus = order.getStatus();
        
        if(currentStatus == Status.DELIVERED || currentStatus == Status.REFUNDED){
            throw new StatusException();
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        OrderResponseWithUserDto response = orderMapper.toOrderResponseWithUserDto(updatedOrder);
        response.setUserInfo(userServiceClient.getUser(updatedOrder.getUserId()));

        return response;
    }

    // create
    @Transactional
    public OrderResponseWithUserDto createOrder(String email, OrderDto orderDto) {

        UserInfoDto user = userServiceClient.getUserByEmail(email);
        if(user == null){
            throw new UserNotFoundException(email);
        }
        Long userId = user.getId();
        
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(Status.PROCESSING);
        order.setCreationDate(new Date());
        order.setOrderItems(new ArrayList<>());

        List<OrderItem> orderItems = getOrderItemsByOrder(orderDto, order);

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        OrderResponseWithUserDto resp = orderMapper.toOrderResponseWithUserDto(savedOrder);
        resp.setUserInfo(userServiceClient.getUser(userId));

        return resp;
    }

    // delete
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // util
    private List<OrderItem> getOrderItemsByOrder(OrderDto orderDto, Order order) {
        List<Long> itemIds = orderDto.getOrderItems().stream()
                .map(OrderItemDto::getItemId)
                .toList();

        if (itemIds.size() != itemIds.stream().distinct().count()) {
            throw new RepeatbleItemException();
        }

        List<Item> items = itemService.getItemByIds(itemIds);

        if (items.size() != itemIds.size()) {
            List<Long> foundItemIds = items.stream().map(Item::getId).toList();
            Long missingItemId = itemIds.stream()
                    .filter(id -> !foundItemIds.contains(id))
                    .findFirst()
                    .orElse(null);
            throw new ItemNotFoundException(missingItemId);
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for (Item item : items) {
            Integer quantity = orderDto.getOrderItems()
                    .stream()
                    .filter(e -> e.getItemId().equals(item.getId()))
                    .map(OrderItemDto::getQuantity)
                    .findFirst()
                    .orElseThrow(() -> new ItemNotFoundException(item.getId()));

            orderItems.add(new OrderItem(null, order, item, quantity));
        }

        return orderItems;
    }

    public Boolean isOrderOwner(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        UserInfoDto user = userServiceClient.getUserByEmail(email);
        if(user == null){
            throw new UserNotFoundException(email);
        }
        
        return order.getUserId() == user.getId(); 
    }

}
