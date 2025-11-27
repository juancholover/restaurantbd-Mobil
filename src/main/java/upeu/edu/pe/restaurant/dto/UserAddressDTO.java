package upeu.edu.pe.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressDTO {
    private Long id;
    private String address;
    private String phone;
    private String label;
    private Boolean isDefault;
    private Double latitude;
    private Double longitude;
}
