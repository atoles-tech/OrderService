package atl.web.order_service.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {
    
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${topic.order-topic}")
    private String topic;

    public MessageProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(OrderEvent orderEvent){
        kafkaTemplate.send(topic, orderEvent);
    } 

}
