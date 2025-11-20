package atl.web.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ValidateTokenRequestDto {

    @NotBlank(message = "Token must not be blank")
    private String token;
}
