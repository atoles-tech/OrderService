package atl.web.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import atl.web.order_service.dto.UserInfoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = "user-service", url = "${user.service.url:http://localhost:8081}")
public interface UserServiceClient {
    
    @GetMapping("/api/v1/users/{id}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    UserInfoDto getUser(@PathVariable Long id);

    default UserInfoDto getUserFallback(Long id, Exception ex){
        return new UserInfoDto(id, null, null, null, null);
    }
}
