package upeu.edu.pe.restaurant.repository;

import upeu.edu.pe.restaurant.entity.Coupon;
import upeu.edu.pe.restaurant.entity.CouponUsage;
import upeu.edu.pe.restaurant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    
    /**
     * Contar usos de un cupón por un usuario
     */
    long countByUserAndCoupon(User user, Coupon coupon);
    
    /**
     * Obtener historial de cupones de un usuario
     */
    List<CouponUsage> findByUserOrderByUsedAtDesc(User user);
    
    /**
     * Verificar si un usuario ya usó un cupón
     */
    boolean existsByUserAndCoupon(User user, Coupon coupon);
}
