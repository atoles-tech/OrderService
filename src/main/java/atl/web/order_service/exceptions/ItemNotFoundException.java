package atl.web.order_service.exceptions;

public class ItemNotFoundException extends RuntimeException{
    
    public ItemNotFoundException(String name){
        super("Item '" + name + "' not found");
    }

    public ItemNotFoundException(Long id){
        super("Item with id="+id+" not found");
    }

}
