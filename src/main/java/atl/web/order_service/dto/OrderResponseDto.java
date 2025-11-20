package atl.web.order_service.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import atl.web.order_service.model.Status;
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
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private Status status;
    private Date creationDate;
    
    @Builder.Default
    private List<OrderItemResponseDto> orderItems = new ArrayList<>();
}
