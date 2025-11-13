package upeu.edu.pe.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.RestaurantDTO;
import upeu.edu.pe.restaurant.entity.Restaurant;
import upeu.edu.pe.restaurant.repository.MenuItemRepository;
import upeu.edu.pe.restaurant.repository.RestaurantRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantEnrichmentService enrichmentService;
    
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        enrichmentService.enrichRestaurants(restaurants);
        
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));
        enrichmentService.enrichRestaurant(restaurant);
        return convertToDTO(restaurant);
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantDTO> searchRestaurants(String keyword) {
        List<Restaurant> restaurants = restaurantRepository.findAll()
                .stream()
                .filter(r -> r.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
        
        enrichmentService.enrichRestaurants(restaurants);
        
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getRestaurantsWithActivePromotions() {
        List<Restaurant> restaurants = restaurantRepository.findActivePromotions(LocalDateTime.now());
        enrichmentService.enrichRestaurants(restaurants);
        
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getRestaurantsByPriceRange(String priceRange) {
        List<Restaurant> restaurants = restaurantRepository.findByPriceRangeAndIsActiveTrue(priceRange);
        enrichmentService.enrichRestaurants(restaurants);
        
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getOpenRestaurants() {
        List<Restaurant> allRestaurants = restaurantRepository.findByIsActiveTrue();
        enrichmentService.enrichRestaurants(allRestaurants);
        
        // Filtrar solo los que están abiertos
        return allRestaurants.stream()
                .filter(r -> r.getIsOpenNow() != null && r.getIsOpenNow())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RestaurantDTO> getRestaurantsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllRestaurants();
        }
        
        List<Restaurant> restaurants = restaurantRepository.findByCategoryContainingIgnoreCase(category.trim());
        enrichmentService.enrichRestaurants(restaurants);
        
        return restaurants.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private RestaurantDTO convertToDTO(Restaurant restaurant) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setDescription(restaurant.getDescription());
        dto.setAddress(restaurant.getAddress());
        dto.setPhone(restaurant.getPhone());
        dto.setRating(restaurant.getRating());
        dto.setImageUrl(restaurant.getImageUrl());
        
        // Campos de ubicación
        dto.setLatitude(restaurant.getLatitude());
        dto.setLongitude(restaurant.getLongitude());
        
        // Campos de delivery
        dto.setDeliveryFee(restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : java.math.BigDecimal.valueOf(2.50));
        dto.setDeliveryTime(restaurant.getDeliveryTime() != null ? restaurant.getDeliveryTime() : 30);
        
        // Campos de promociones
        dto.setHasPromotion(restaurant.getHasPromotion() != null ? restaurant.getHasPromotion() : false);
        dto.setPromotionTitle(restaurant.getPromotionTitle());
        dto.setPromotionDescription(restaurant.getPromotionDescription());
        dto.setDiscountPercentage(restaurant.getDiscountPercentage());
        
        // Campos de precio
        dto.setPriceRange(restaurant.getPriceRange() != null ? restaurant.getPriceRange() : "$$");
        dto.setMinPrice(restaurant.getMinPrice());
        dto.setMaxPrice(restaurant.getMaxPrice());
        dto.setAveragePrice(restaurant.getAveragePrice());
        
        // Campos calculados dinámicamente
        dto.setIsOpenNow(restaurant.getIsOpenNow() != null ? restaurant.getIsOpenNow() : true);
        dto.setTodaySchedule(restaurant.getTodaySchedule() != null ? restaurant.getTodaySchedule() : restaurant.getOpeningHours());
        dto.setReviewCount(restaurant.getReviewCount() != null ? restaurant.getReviewCount() : 0);
        dto.setDistanceKm(restaurant.getDistanceKm());
        
        // Categorías
        dto.setCategories(restaurant.getCategories() != null && !restaurant.getCategories().isEmpty() 
            ? new ArrayList<>(restaurant.getCategories()) 
            : List.of("Restaurant"));
        
        dto.setIsFavorite(false);
        
        // Contar productos del restaurante
        int productCount = menuItemRepository.findByRestaurantId(restaurant.getId()).size();
        dto.setTotalProducts(productCount);
        
        return dto;
    }
    
    @Transactional
    public RestaurantDTO createRestaurant(Restaurant restaurant) {
        restaurant.setIsActive(true);
        restaurant.setRating(java.math.BigDecimal.ZERO);
        Restaurant saved = restaurantRepository.save(restaurant);
        return convertToDTO(saved);
    }
    
    @Transactional
    public RestaurantDTO updateRestaurant(Long id, Restaurant restaurant) {
        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));
        
        existing.setName(restaurant.getName());
        existing.setDescription(restaurant.getDescription());
        existing.setAddress(restaurant.getAddress());
        existing.setPhone(restaurant.getPhone());
        existing.setImageUrl(restaurant.getImageUrl());
        
        Restaurant updated = restaurantRepository.save(existing);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) {
            throw new RuntimeException("Restaurante no encontrado");
        }
        restaurantRepository.deleteById(id);
    }
}
