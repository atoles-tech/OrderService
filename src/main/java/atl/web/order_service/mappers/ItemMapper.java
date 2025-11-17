package atl.web.order_service.mappers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import atl.web.order_service.dto.ItemDto;
import atl.web.order_service.dto.ItemResponseDto;
import atl.web.order_service.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    
    ItemResponseDto toItemResponseDto(Item item);
    
    @Mapping(target = "id", ignore = true) 
    Item toItem(ItemDto itemDto);

    List<ItemResponseDto> toItemResponseDtoList(List<Item> items);

    List<Item> toItemList(List<ItemDto> itemDtos);
    
    default Page<ItemResponseDto> toItemResponseDtoPage(Page<Item> items) {
        if (items == null) {
            return Page.empty();
        }

        List<ItemResponseDto> content = items.getContent()
                .stream()
                .map(this::toItemResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(
                content,
                items.getPageable(),
                items.getTotalElements());
    }

    default Optional<ItemResponseDto> toOptionalItemResponseDto(Optional<Item> item){
        if(!item.isPresent()){
            return Optional.empty();
        }

        return Optional.of(toItemResponseDto(item.get()));
    }

}
