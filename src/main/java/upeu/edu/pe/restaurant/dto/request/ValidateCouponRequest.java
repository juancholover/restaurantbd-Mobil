package upeu.edu.pe.restaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request para validar un cupón
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCouponRequest {
    
    @NotBlank(message = "El código del cupón es requerido")
    private String code;
    
    @NotNull(message = "El total del carrito es requerido")
    @Positive(message = "El total debe ser mayor a 0")
    private BigDecimal cartTotal;
}
