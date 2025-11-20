package atl.web.order_service.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import atl.web.order_service.model.Order;
import atl.web.order_service.model.Status;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Page<Order> findAll(Pageable pageable);

    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByStatus(Status status);
    Page<Order> findByStatus(Status status, Pageable pageable);

}
