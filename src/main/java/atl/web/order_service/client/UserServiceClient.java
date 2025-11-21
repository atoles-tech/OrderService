package atl.web.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import atl.web.order_service.dto.UserInfoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/api/v1/users/{id}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    UserInfoDto getUser(@PathVariable Long id);

    @GetMapping("api/v1/users")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallbackByEmail")
    UserInfoDto getUserByEmail(@RequestParam String email);

    default UserInfoDto getUserFallback(Long id, Exception ex){
        return null;
    }

    default UserInfoDto getUserFallbackByEmail(String email, Exception ex){
        return null;
    }
}
