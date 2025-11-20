package atl.web.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemResponseDto {
    private Long id;
    private Long orderId; // i use long, because user must not know what order it is 
    private ItemResponseDto item;
    private Integer quantity;
}
