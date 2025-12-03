package atl.web.order_service.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import atl.web.order_service.model.Status;
import atl.web.order_service.services.OrderService;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MessageConsumer {
    
    private OrderService orderService;

    @KafkaListener(topics = "${topic.payment-topic}", properties = {"spring.json.value.default.type=atl.web.order_service.kafka.consumer.PaymentEvent"})
    public void listenPayments(PaymentEvent event){
        orderService.updateOrderStatus(event.getOrderId(), 
            event.getStatus().equalsIgnoreCase("success")?Status.SUCCESS:Status.FAILED);
    }

}
