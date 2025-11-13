package upeu.edu.pe.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Coupon para sistema de cupones de descuento
 */
@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code; // DESCUENTO10, BIENVENIDA20, etc.
    
    @Column(nullable = false)
    private String description; // "10% de descuento en tu primera compra"
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // FIXED (monto fijo) o PERCENTAGE (porcentaje)
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue; // 10.00 (S/10 o 10%)
    
    @Column(precision = 10, scale = 2)
    private BigDecimal minimumAmount; // Monto mínimo de compra para aplicar
    
    @Column(precision = 10, scale = 2)
    private BigDecimal maximumDiscount; // Descuento máximo si es porcentaje
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column
    private LocalDateTime expiresAt; // Fecha de expiración
    
    @Column
    private Integer usageLimit; // Límite total de usos (null = ilimitado)
    
    @Column
    @Builder.Default
    private Integer usageCount = 0; // Veces que se ha usado
    
    @Column
    private Integer userUsageLimit; // Veces que un usuario puede usarlo (null = ilimitado)
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * Tipos de descuento
     */
    public enum DiscountType {
        FIXED,      // Descuento de monto fijo (ej: S/10)
        PERCENTAGE  // Descuento porcentual (ej: 10%)
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica si el cupón está vencido
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Verifica si el cupón alcanzó su límite de usos
     */
    public boolean hasReachedUsageLimit() {
        return usageLimit != null && usageCount >= usageLimit;
    }
    
    /**
     * Verifica si el cupón es válido
     */
    public boolean isValid() {
        return isActive && !isExpired() && !hasReachedUsageLimit();
    }
    
    /**
     * Calcula el descuento para un monto dado
     */
    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!isValid() || amount.compareTo(getMinimumAmount()) < 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount;
        if (discountType == DiscountType.FIXED) {
            discount = discountValue;
        } else {
            // Porcentaje
            discount = amount.multiply(discountValue).divide(new BigDecimal(100));
            
            // Aplicar descuento máximo si existe
            if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) {
                discount = maximumDiscount;
            }
        }
        
        // El descuento no puede ser mayor al monto
        return discount.min(amount);
    }
    
    public BigDecimal getMinimumAmount() {
        return minimumAmount != null ? minimumAmount : BigDecimal.ZERO;
    }
}
