package upeu.edu.pe.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {
    
    private Long id;
    
    // orderId es OPCIONAL - null si la reseña no está asociada a un pedido
    private Long orderId;
    
    private Long userId;
    
    // userName se obtiene de la relación User (solo lectura)
    private String userName;
    
    @NotNull(message = "El ID del restaurante es requerido")
    private Long restaurantId;
    
    // restaurantName se obtiene de la relación Restaurant (solo lectura)
    private String restaurantName;
    
    @NotNull(message = "La calificación es requerida")
    @DecimalMin(value = "0.0", message = "La calificación mínima es 0.0")
    @DecimalMax(value = "5.0", message = "La calificación máxima es 5.0")
    private BigDecimal rating;
    
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comment;
    
    private List<String> images;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
