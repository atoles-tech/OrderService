package atl.web.order_service.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import atl.web.order_service.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAll(Pageable pageable);
    
    List<Item> findByName(String name);

    Boolean existsByName(String name);

}
