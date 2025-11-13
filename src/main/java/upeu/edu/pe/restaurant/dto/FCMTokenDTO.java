package upeu.edu.pe.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FCMTokenDTO {
    
    @NotBlank(message = "El token es requerido")
    private String token;
    
    @NotBlank(message = "El tipo de dispositivo es requerido")
    private String deviceType; // android, ios, web
    
    private String deviceName;
}
