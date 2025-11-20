package atl.web.order_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import atl.web.order_service.dto.ItemResponseDto;
import atl.web.order_service.dto.UpdateItemDto;
import atl.web.order_service.exceptions.ItemNotFoundException;
import atl.web.order_service.mappers.ItemMapper;
import atl.web.order_service.model.Item;
import atl.web.order_service.repositories.ItemRepository;
import atl.web.order_service.services.ItemService;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;
    
    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemService itemService;

    private Item item;
    private ItemResponseDto itemResponseDto;

    @BeforeEach
    public void init(){
        item = new Item(1L, "name", 123.);
        itemResponseDto = new ItemResponseDto(1L, "name", 123.);
    }

    @Test
    @DisplayName("Should return item when it exists")
    public void shouldReturnItem_WhenItExists(){
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponseDto(item)).thenReturn(itemResponseDto);
    
        ItemResponseDto response = itemService.getById(1L);

        assertEquals(itemResponseDto, response);
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    public void shouldThrowException_WhenItemNotFound(){
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
    
        assertThrows(ItemNotFoundException.class, () -> itemService.getById(1L));
    }

    @Test
    @DisplayName("Should return updated item when it exists")
    public void shouldReturnUpdatedItem_WhenItExists(){
        UpdateItemDto updateItemDto = new UpdateItemDto("item", 123.);
        ItemResponseDto newItemResponseDto = new ItemResponseDto(1L, "item", 123.); 
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponseDto(item)).thenReturn(newItemResponseDto);
        when(itemRepository.save(item)).thenReturn(item);

        ItemResponseDto response = itemService.updateItem(updateItemDto, 1L);

        assertEquals(newItemResponseDto, response);
    }

    @Test
    @DisplayName("Should throw exception when item not found")
    public void shouldThrowExceptionInUpdate_WhenItNotFound(){
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,() -> itemService.updateItem(new UpdateItemDto("item",123.), 1L));
    }

}
