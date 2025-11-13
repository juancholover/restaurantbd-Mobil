package upeu.edu.pe.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private Long userId; // null para enviar a todos
    
    @NotBlank(message = "El tipo de notificación es requerido")
    private String type; // order_status, special_offer, new_restaurant, general
    
    @NotBlank(message = "El título es requerido")
    private String title;
    
    @NotBlank(message = "El cuerpo es requerido")
    private String body;
    
    private Map<String, String> data; // Datos adicionales
}
