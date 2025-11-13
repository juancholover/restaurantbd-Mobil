package upeu.edu.pe.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String address;
    
    private String phone;
    
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;
    
    private String imageUrl;
    
    @Builder.Default
    private Boolean isActive = true;
    
    // Campos de ubicación y precios
    @Column(columnDefinition = "DECIMAL(10, 8)")
    private Double latitude;
    
    @Column(columnDefinition = "DECIMAL(11, 8)")
    private Double longitude;
    
    @Column(name = "average_price", precision = 10, scale = 2)
    private BigDecimal averagePrice;
    
    @Column(name = "opening_hours", length = 500)
    @Builder.Default
    private String openingHours = "9:00 AM - 10:00 PM";
    
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;
    
    @Column(name = "delivery_time")
    private Integer deliveryTime; // minutos estimados
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "restaurant_categories", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Column(name = "category")
    @Builder.Default
    private Set<String> categories = new HashSet<>();
    
    // Campos de promociones
    @Column(name = "has_promotion")
    @Builder.Default
    private Boolean hasPromotion = false;
    
    @Column(name = "promotion_title", length = 255)
    private String promotionTitle;
    
    @Column(name = "promotion_description", columnDefinition = "TEXT")
    private String promotionDescription;
    
    @Column(name = "discount_percentage")
    private Integer discountPercentage;
    
    @Column(name = "promotion_start_date")
    private LocalDateTime promotionStartDate;
    
    @Column(name = "promotion_end_date")
    private LocalDateTime promotionEndDate;
    
    // Campos de rango de precio
    @Column(name = "price_range", length = 10)
    @Builder.Default
    private String priceRange = "$$";
    
    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice;
    
    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice;
    
    // Campos transientes (calculados en tiempo de ejecución)
    @Transient
    private Boolean isOpenNow;
    
    @Transient
    private String todaySchedule;
    
    @Transient
    private Integer reviewCount;
    
    @Transient
    private Double distanceKm;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<MenuItem> menuItems = new HashSet<>();
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Reservation> reservations = new HashSet<>();
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Order> orders = new HashSet<>();
}
