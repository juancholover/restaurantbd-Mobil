package upeu.edu.pe.restaurant.repository;

import upeu.edu.pe.restaurant.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    /**
     * Buscar cupón por código (case-insensitive)
     */
    Optional<Coupon> findByCodeIgnoreCase(String code);
    
    /**
     * Buscar cupones activos
     */
    List<Coupon> findByIsActiveTrue();
    
    /**
     * Buscar cupones activos y no expirados
     */
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    List<Coupon> findActiveCoupons(LocalDateTime now);
    
    /**
     * Verificar si un código existe
     */
    boolean existsByCodeIgnoreCase(String code);
}
