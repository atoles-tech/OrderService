package atl.web.order_service.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import atl.web.order_service.dto.ItemDto;
import atl.web.order_service.dto.ItemResponseDto;
import atl.web.order_service.dto.UpdateItemDto;
import atl.web.order_service.exceptions.ItemNotFoundException;
import atl.web.order_service.mappers.ItemMapper;
import atl.web.order_service.model.Item;
import atl.web.order_service.repositories.ItemRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    
    private ItemRepository itemRepository;
    private ItemMapper itemMapper;
    
    //read
    public Page<ItemResponseDto> getAll(Pageable pageable){
        return itemMapper.toItemResponseDtoPage(itemRepository.findAll(pageable));
    }

    public List<ItemResponseDto> getAll(){
        return itemMapper.toItemResponseDtoList(itemRepository.findAll());
    }

    public List<ItemResponseDto> getByName(String name){
        return itemMapper.toItemResponseDtoList(itemRepository.findByName(name));
    }

    public ItemResponseDto getById(Long id){
        return itemMapper.toItemResponseDto(itemRepository.findById(id)
            .orElseThrow(()-> new ItemNotFoundException(id)));
    }

    public Boolean existsByName(String name){
        return itemRepository.existsByName(name);
    }

    public Boolean existsById(Long id){
        return itemRepository.existsById(id);
    }

    //update
    @Transactional
    public ItemResponseDto updateItem(UpdateItemDto updateItemDto, Long id){
        Item item = itemRepository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
   
        if(updateItemDto.getName() != null) item.setName(updateItemDto.getName());
        if(updateItemDto.getPrice() != null) item.setPrice(updateItemDto.getPrice());

        return itemMapper.toItemResponseDto(itemRepository.save(item));
    }

    //create
    @Transactional
    public ItemResponseDto createItem(ItemDto item){
        return itemMapper.toItemResponseDto(itemRepository.save(itemMapper.toItem(item)));
    }
    
    //delete
    @Transactional
    public void deleteItem(Long id){
        if(!existsById(id)){
            throw new ItemNotFoundException(id);
        }
        itemRepository.deleteById(id);
    }

    //entity
    public List<Item> getItemByIds(List<Long> ids){
        return itemRepository.findAllById(ids);
    }
}
