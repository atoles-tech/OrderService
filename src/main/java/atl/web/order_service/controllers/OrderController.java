package atl.web.order_service.controllers;

import java.security.Principal;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import atl.web.order_service.dto.OrderDto;
import atl.web.order_service.dto.OrderResponseWithUserDto;
import atl.web.order_service.model.Status;
import atl.web.order_service.services.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private OrderService orderService;

    @GetMapping("/orders")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrdersByStatus(
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "0") Integer size,
            @RequestParam(defaultValue = "creationDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        if (status != null) {
            if (size == 0) {
                return ResponseEntity.ok(orderService.getByStatus(status));
            }

            Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

            return ResponseEntity.ok(orderService.getByStatus(status, PageRequest.of(page, size, sort)));
        }

        if (size == 0) {
            return ResponseEntity.ok(orderService.getAll());
        }

        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return ResponseEntity.ok(orderService.getAll(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/users/{userId}/orders")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and #userId.toString() == authentication.name)")
    public ResponseEntity<?> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "creationDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        return ResponseEntity.ok(orderService.getAllByUserId(userId, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and @orderService.isOrderOwner(#id, authentication.principal))")
    public ResponseEntity<OrderResponseWithUserDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PutMapping("orders/{id}/items")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and @orderService.isOrderOwner(#id, authentication.principal))")
    public ResponseEntity<OrderResponseWithUserDto> updateOrderItems(@PathVariable Long id, @RequestBody @Valid OrderDto orderDto){
        return ResponseEntity.ok(orderService.updateOrder(id, orderDto));
    }

    @PutMapping("orders/{id}/status")
    @PreAuthorize(value = "hasRole('ADMIN') or (hasRole('USER') and @orderService.isOrderOwner(#id, authentication.principal))")
    public ResponseEntity<OrderResponseWithUserDto> updateOrderStatus(@PathVariable Long id, @RequestParam Status status){
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<OrderResponseWithUserDto> createOrder(
            @RequestBody @Valid OrderDto orderDto,
            Principal principal){    
        return ResponseEntity.ok(orderService.createOrder(principal.getName(), orderDto));
    }

    @DeleteMapping("orders/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id){
        orderService.deleteOrder(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
