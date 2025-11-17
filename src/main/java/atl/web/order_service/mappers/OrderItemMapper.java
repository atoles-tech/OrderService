package atl.web.order_service.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import atl.web.order_service.dto.OrderItemDto;
import atl.web.order_service.dto.OrderItemResponseDto;
import atl.web.order_service.model.OrderItem;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface OrderItemMapper {
    
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    OrderItem toOrderItem(OrderItemDto orderItemDto);

    @Mapping(source = "orderItem.order.id", target = "orderId")
    @Mapping(source = "orderItem.item", target = "item")
    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);

    List<OrderItem> toOrderItemList(List<OrderItemDto> orderItemDtos);
    List<OrderItemResponseDto> toOrderItemResponseList(List<OrderItem> orderItems);

}
