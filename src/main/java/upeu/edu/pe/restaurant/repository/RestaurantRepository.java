package upeu.edu.pe.restaurant.repository;

import upeu.edu.pe.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByIsActiveTrue();
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    
    // Consultas para promociones
    List<Restaurant> findByHasPromotionTrueAndIsActiveTrue();
    
    @Query("SELECT r FROM Restaurant r WHERE r.hasPromotion = true AND r.isActive = true " +
           "AND r.promotionStartDate <= :now AND r.promotionEndDate >= :now")
    List<Restaurant> findActivePromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Restaurant r WHERE r.hasPromotion = true AND r.isActive = true " +
           "AND r.discountPercentage >= :minDiscount")
    List<Restaurant> findPromotionsWithMinDiscount(@Param("minDiscount") Integer minDiscount);
    
    // Consultas para rangos de precio
    List<Restaurant> findByPriceRangeAndIsActiveTrue(String priceRange);
    
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true " +
           "AND r.minPrice >= :minPrice AND r.maxPrice <= :maxPrice")
    List<Restaurant> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                      @Param("maxPrice") BigDecimal maxPrice);
    
    // Consultas para ubicación (ya existentes con geolocalización)
    @Query("SELECT r FROM Restaurant r WHERE r.isActive = true " +
           "AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL")
    List<Restaurant> findAllWithLocation();
    
    // Consulta para filtrar por categoría (INSENSITIVE a mayúsculas/minúsculas)
    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.categories c " +
           "WHERE LOWER(c) LIKE LOWER(CONCAT('%', :category, '%')) AND r.isActive = true")
    List<Restaurant> findByCategoryContainingIgnoreCase(@Param("category") String category);
}
