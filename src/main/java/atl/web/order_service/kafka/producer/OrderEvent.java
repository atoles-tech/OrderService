package atl.web.order_service.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    
    private Long orderId;
    private Long userId;
    private Double paymentAmount;

}
