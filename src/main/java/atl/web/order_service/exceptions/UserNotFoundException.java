package atl.web.order_service.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String email){
        super("User with email '" + email+ "' not found");
    }
}
