package upeu.edu.pe.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad para rastrear el uso de cupones por usuario
 */
@Entity
@Table(name = "coupon_usage", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "coupon_id", "order_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // Null si solo se validó pero no se usó en orden
    
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime usedAt = LocalDateTime.now();
}
