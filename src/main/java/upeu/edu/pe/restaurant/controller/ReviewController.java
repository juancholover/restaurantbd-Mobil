package upeu.edu.pe.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.ReviewDTO;
import upeu.edu.pe.restaurant.dto.ReviewStatisticsDTO;
import upeu.edu.pe.restaurant.security.UserPrincipal;
import upeu.edu.pe.restaurant.service.ReviewService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * POST /api/reviews - Crear reseña
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        ReviewDTO createdReview = reviewService.createReview(
                reviewDTO, 
                currentUser.getId(), 
                currentUser.getUsername()
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reseña creada exitosamente");
        response.put("data", createdReview);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * GET /api/reviews/restaurant/{restaurantId} - Obtener reseñas de restaurante
     */
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Map<String, Object>> getRestaurantReviews(
            @PathVariable Long restaurantId) {
        
        List<ReviewDTO> reviews = reviewService.getReviewsByRestaurantId(restaurantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reseñas obtenidas exitosamente");
        response.put("data", reviews);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/reviews/restaurant/{restaurantId}/statistics - Obtener estadísticas
     */
    @GetMapping("/restaurant/{restaurantId}/statistics")
    public ResponseEntity<Map<String, Object>> getRestaurantStatistics(
            @PathVariable Long restaurantId) {
        
        ReviewStatisticsDTO statistics = reviewService.getRestaurantStatistics(restaurantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Estadísticas obtenidas exitosamente");
        response.put("data", statistics);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/reviews/my - Obtener mis reseñas
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reseñas obtenidas exitosamente");
        response.put("data", reviews);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/reviews/order/{orderId}/check - Verificar si pedido tiene reseña
     */
    @GetMapping("/order/{orderId}/check")
    public ResponseEntity<Map<String, Object>> checkOrderReview(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        Map<String, Object> result = reviewService.checkOrderHasReview(orderId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.putAll(result);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/reviews/{id} - Obtener reseña por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReviewById(@PathVariable Long id) {
        ReviewDTO review = reviewService.getReviewById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", review);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /api/reviews/{id} - Actualizar reseña
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reseña actualizada exitosamente");
        response.put("data", updatedReview);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/reviews/{id} - Eliminar reseña
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        reviewService.deleteReview(id, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reseña eliminada exitosamente");
        
        return ResponseEntity.ok(response);
    }
}
