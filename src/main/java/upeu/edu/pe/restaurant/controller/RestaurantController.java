package upeu.edu.pe.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.RestaurantDTO;
import upeu.edu.pe.restaurant.dto.response.ApiResponse;
import upeu.edu.pe.restaurant.entity.Restaurant;
import upeu.edu.pe.restaurant.service.RestaurantService;
import upeu.edu.pe.restaurant.service.LocationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    
    private final RestaurantService restaurantService;
    private final LocationService locationService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getAllRestaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        
        List<RestaurantDTO> restaurants;
        
        if (category != null && !category.isEmpty()) {
            // Filtrar por categoría
            restaurants = restaurantService.getRestaurantsByCategory(category);
        } else if (search != null && !search.isEmpty()) {
            // Buscar por nombre
            restaurants = restaurantService.searchRestaurants(search);
        } else {
            // Obtener todos
            restaurants = restaurantService.getAllRestaurants();
        }
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurantes obtenidos exitosamente", restaurants)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurantById(@PathVariable Long id) {
        RestaurantDTO restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurante obtenido exitosamente", restaurant)
        );
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantDTO>> createRestaurant(
            @RequestBody Restaurant restaurant) {
        RestaurantDTO created = restaurantService.createRestaurant(restaurant);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurante creado exitosamente", created)
        );
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDTO>> updateRestaurant(
            @PathVariable Long id,
            @RequestBody Restaurant restaurant) {
        RestaurantDTO updated = restaurantService.updateRestaurant(id, restaurant);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurante actualizado exitosamente", updated)
        );
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurante eliminado exitosamente", null)
        );
    }
    
    /**
     * GET /api/restaurants/nearby?lat=19.432608&lng=-99.133209&radius=5
     * Buscar restaurantes cercanos a una ubicación
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getNearbyRestaurants(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "10") Double radius,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String category) {
        
        List<Map<String, Object>> restaurants = locationService.searchRestaurants(
                lat, lng, radius, minRating, category
        );
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurantes cercanos obtenidos", restaurants)
        );
    }
    
    /**
     * GET /api/restaurants/{id}/directions?from_lat=19.432608&from_lng=-99.133209
     * Obtener direcciones a un restaurante
     */
    @GetMapping("/{id}/directions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDirections(
            @PathVariable Long id,
            @RequestParam(name = "from_lat") Double fromLat,
            @RequestParam(name = "from_lng") Double fromLng) {
        
        Map<String, Object> directions = locationService.getDirections(id, fromLat, fromLng);
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Direcciones obtenidas", directions)
        );
    }
    
    /**
     * GET /api/restaurants/promotions
     * Obtener restaurantes con promociones activas
     */
    @GetMapping("/promotions")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getRestaurantsWithPromotions() {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsWithActivePromotions();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurantes con promociones obtenidos", restaurants)
        );
    }
    
    /**
     * GET /api/restaurants/price-range/{range}
     * Filtrar restaurantes por rango de precio ($, $$, $$$, $$$$)
     */
    @GetMapping("/price-range/{range}")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getRestaurantsByPriceRange(
            @PathVariable String range) {
        List<RestaurantDTO> restaurants = restaurantService.getRestaurantsByPriceRange(range);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurantes filtrados por precio", restaurants)
        );
    }
    
    /**
     * GET /api/restaurants/open-now
     * Obtener solo restaurantes que están abiertos ahora
     */
    @GetMapping("/open-now")
    public ResponseEntity<ApiResponse<List<RestaurantDTO>>> getOpenRestaurants() {
        List<RestaurantDTO> restaurants = restaurantService.getOpenRestaurants();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Restaurantes abiertos obtenidos", restaurants)
        );
    }
}
