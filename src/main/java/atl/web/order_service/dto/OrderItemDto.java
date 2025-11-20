package atl.web.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class OrderItemDto {
    @NotNull(message = "Item id can't be null")
    private Long itemId;

    @Min(value = 1, message = "Quantity can't be less 1")
    private Integer quantity;
}
