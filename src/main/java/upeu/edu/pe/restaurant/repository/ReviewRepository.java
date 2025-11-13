package upeu.edu.pe.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.restaurant.entity.Review;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Verificar si existe reseña para un pedido
    boolean existsByOrderId(Long orderId);
    
    // Obtener reseña por pedido
    Optional<Review> findByOrderId(Long orderId);
    
    // Obtener reseñas de un restaurante
    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    
    // Obtener reseñas de un usuario
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Contar reseñas de un restaurante
    long countByRestaurantId(Long restaurantId);
    
    // Calcular rating promedio de un restaurante
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurantId = :restaurantId")
    BigDecimal calculateAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);
    
    // Obtener distribución de ratings (contar cuántos de cada rating)
    @Query("SELECT FLOOR(r.rating) as rating, COUNT(r) as count FROM Review r WHERE r.restaurantId = :restaurantId GROUP BY FLOOR(r.rating)")
    List<Object[]> getRatingDistribution(@Param("restaurantId") Long restaurantId);
    
    // Obtener reseñas recientes (últimas N reseñas)
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews();
    
    // Obtener reseñas con rating específico o mayor
    @Query("SELECT r FROM Review r WHERE r.restaurantId = :restaurantId AND r.rating >= :minRating ORDER BY r.createdAt DESC")
    List<Review> findByRestaurantIdAndMinRating(@Param("restaurantId") Long restaurantId, @Param("minRating") BigDecimal minRating);
    
    // Obtener reseñas sin pedido asociado (reseñas generales)
    List<Review> findByOrderIdIsNull();
    
    // Obtener reseñas con pedido asociado (compras verificadas)
    List<Review> findByOrderIdIsNotNull();
    
    // Obtener reseñas generales de un restaurante
    List<Review> findByRestaurantIdAndOrderIdIsNullOrderByCreatedAtDesc(Long restaurantId);
    
    // Obtener reseñas verificadas de un restaurante
    List<Review> findByRestaurantIdAndOrderIdIsNotNullOrderByCreatedAtDesc(Long restaurantId);
}
