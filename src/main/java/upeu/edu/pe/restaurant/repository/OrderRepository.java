package upeu.edu.pe.restaurant.repository;

import upeu.edu.pe.restaurant.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Obtener pedidos por usuario (relación ManyToOne)
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Obtener pedidos por restaurante
    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId ORDER BY o.createdAt DESC")
    List<Order> findByRestaurantIdOrderByCreatedAtDesc(@Param("restaurantId") Long restaurantId);
    
    // Obtener pedidos por usuario y estado
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.Status status);
    
    // Contar pedidos por usuario
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // Contar pedidos por restaurante
    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurant.id = :restaurantId")
    long countByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    // ==================== MÉTODOS PARA ADMIN DASHBOARD ====================
    
    /**
     * Sumar total de órdenes por estado
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status = :status")
    Double sumTotalAmountByStatus(@Param("status") Order.Status status);
    
    /**
     * Contar usuarios distintos con órdenes
     */
    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o")
    long countDistinctUsers();
    
    /**
     * Obtener órdenes recientes ordenadas por fecha
     */
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Buscar orden por Payment Intent ID
     */
    @Query("SELECT o FROM Order o WHERE o.paymentIntentId = :paymentIntentId")
    java.util.Optional<Order> findByPaymentIntentId(@Param("paymentIntentId") String paymentIntentId);
}
