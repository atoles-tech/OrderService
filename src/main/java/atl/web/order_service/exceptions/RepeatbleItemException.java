package atl.web.order_service.exceptions;

public class RepeatbleItemException extends RuntimeException{
    public RepeatbleItemException(){
        super("Don't take several times one item"); //TODO:
    }
}
