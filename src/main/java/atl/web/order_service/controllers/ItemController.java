package atl.web.order_service.controllers;

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

import atl.web.order_service.dto.ItemDto;
import atl.web.order_service.dto.ItemResponseDto;
import atl.web.order_service.dto.UpdateItemDto;
import atl.web.order_service.services.ItemService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/items")
@AllArgsConstructor
public class ItemController {
    
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<?> getAll(
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "0") Integer size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "asc") String direction
    ){
        if(name != null && !name.isEmpty()){
            return ResponseEntity.ok(itemService.getByName(name));
        }

        if(size == 0){
            return ResponseEntity.ok(itemService.getAll());
        }

        Sort sort = direction.equals("asc")
            ?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();

        return ResponseEntity.ok(itemService.getAll(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getById(@PathVariable Long id){
        return ResponseEntity.ok(itemService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id, @RequestBody @Valid UpdateItemDto updateItemDto){
        return ResponseEntity.ok(itemService.updateItem(updateItemDto, id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ItemResponseDto> createItem(@RequestBody @Valid ItemDto itemDto){
        return ResponseEntity.ok(itemService.createItem(itemDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id){
        itemService.deleteItem(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
