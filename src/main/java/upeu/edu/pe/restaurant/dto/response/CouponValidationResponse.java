package upeu.edu.pe.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta de validación de cupón
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResponse {
    
    private Boolean valid;
    private String code;
    private String message;
    private BigDecimal discount;
    private String discountType; // "fixed" o "percentage"
    private BigDecimal discountValue; // Valor original del descuento
    private BigDecimal finalTotal; // Total después del descuento
    private LocalDateTime expiresAt;
    private String reason; // "expired", "used", "min_amount", etc.
}
