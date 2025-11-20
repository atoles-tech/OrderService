package atl.web.order_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UpdateItemDto {
    @Size(min = 5, max = 100, message = "Size of name must be between 5 and 100")
    private String name;

    @DecimalMin(value = "0.01", message = "Price can't be less than 0.01")
    private Double price;
}
