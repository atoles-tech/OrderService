package atl.web.order_service.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.order_service.dto.OrderDto;
import atl.web.order_service.dto.OrderResponseDto;
import atl.web.order_service.dto.OrderResponseWithUserDto;
import atl.web.order_service.dto.UserInfoDto;
import atl.web.order_service.model.Order;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "creationDate", expression = "java(new java.util.Date())")
    Order toOrder(OrderDto orderDto);

    OrderResponseDto toOrderResponseDto(Order order);

    @Mapping(target = "userInfo", ignore = true)
    OrderResponseWithUserDto toOrderResponseWithUserDto(Order order);

    List<OrderResponseDto> toOrderResponseList(List<Order> orders);

    default Page<OrderResponseDto> toOrderResponseDtoPage(Page<Order> orders) {
        if (orders == null) {
            return Page.empty();
        }

        List<OrderResponseDto> content = orders.getContent()
                .stream()
                .map(this::toOrderResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(
                content,
                orders.getPageable(),
                orders.getTotalElements());
    }

    default Page<OrderResponseWithUserDto> toOrderResponseWithUserDtoPage(Page<Order> orders, UserInfoDto userInfo) {
        if (orders == null) {
            return Page.empty();
        }

        List<OrderResponseWithUserDto> content = orders.getContent()
                .stream()
                .map(this::toOrderResponseWithUserDto)
                .peek((e) -> e.setUserInfo(userInfo))
                .collect(Collectors.toList());

        return new PageImpl<>(
                content,
                orders.getPageable(),
                orders.getTotalElements());
    }
    
}
