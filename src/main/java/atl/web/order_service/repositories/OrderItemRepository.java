package atl.web.order_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import atl.web.order_service.model.Order;
import atl.web.order_service.model.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{

    List<OrderItem> findByOrder(Order order);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

} 
