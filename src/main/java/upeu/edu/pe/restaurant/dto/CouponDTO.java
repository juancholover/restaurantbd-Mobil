package upeu.edu.pe.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para Cupón
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    
    private Long id;
    private String code;
    private String description;
    private String discountType; // "FIXED" o "PERCENTAGE"
    private BigDecimal discountValue;
    private BigDecimal minimumAmount;
    private BigDecimal maximumDiscount;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer userUsageLimit;
    private LocalDateTime createdAt;
}
