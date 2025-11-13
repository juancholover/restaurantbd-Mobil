package upeu.edu.pe.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private BigDecimal rating;
    private String imageUrl;
    
    // Campos de delivery
    private BigDecimal deliveryFee;
    private Integer deliveryTime;
    
    // Campos de ubicación
    private Double latitude;
    private Double longitude;
    
    // Campos de promociones
    private Boolean hasPromotion;
    private String promotionTitle;
    private String promotionDescription;
    private Integer discountPercentage;
    
    // Campos de precio
    private String priceRange;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal averagePrice;
    
    // Campos calculados
    private Boolean isOpenNow;
    private String todaySchedule;
    private Integer reviewCount;
    private Double distanceKm;
    
    // Otros
    private List<String> categories;
    private Boolean isFavorite;
    private Integer totalProducts;
}
