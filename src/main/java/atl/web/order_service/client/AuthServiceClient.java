package atl.web.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import atl.web.order_service.dto.ValidateTokenRequestDto;
import jakarta.validation.Valid;

@FeignClient(
    name = "auth-service"
)
public interface AuthServiceClient {
    
    @PostMapping("/api/v1/auth/validate")
    Boolean validateToken(@RequestBody @Valid ValidateTokenRequestDto request);

    @PostMapping("/api/v1/auth/extract-role")
    String extractRole(@RequestBody @Valid ValidateTokenRequestDto request);

    @PostMapping("/api/v1/auth/extract-email")
    String exrtactEmail(@RequestBody @Valid ValidateTokenRequestDto request);
}
