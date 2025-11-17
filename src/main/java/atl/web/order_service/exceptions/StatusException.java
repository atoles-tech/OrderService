package atl.web.order_service.exceptions;

public class StatusException extends RuntimeException{

    public StatusException(){
        super("The order status can't be changed");
    }
}
