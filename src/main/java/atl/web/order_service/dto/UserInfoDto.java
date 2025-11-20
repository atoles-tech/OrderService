package atl.web.order_service.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserInfoDto {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
