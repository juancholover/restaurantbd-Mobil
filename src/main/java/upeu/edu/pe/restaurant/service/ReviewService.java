package upeu.edu.pe.restaurant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.ReviewDTO;
import upeu.edu.pe.restaurant.dto.ReviewStatisticsDTO;
import upeu.edu.pe.restaurant.entity.Review;
import upeu.edu.pe.restaurant.exception.BadRequestException;
import upeu.edu.pe.restaurant.exception.DuplicateResourceException;
import upeu.edu.pe.restaurant.exception.ResourceNotFoundException;
import upeu.edu.pe.restaurant.repository.ReviewRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Crear una nueva reseña
     */
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO, Long userId, String userName) {
        // Verificar que no exista una reseña previa para este pedido (solo si orderId no es null)
        if (reviewDTO.getOrderId() != null && reviewRepository.existsByOrderId(reviewDTO.getOrderId())) {
            throw new DuplicateResourceException("Este pedido ya tiene una reseña");
        }
        
        // Validar rating
        if (reviewDTO.getRating().compareTo(BigDecimal.ZERO) < 0 || 
            reviewDTO.getRating().compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BadRequestException("La calificación debe estar entre 0.0 y 5.0");
        }
        
        Review review = new Review();
        review.setOrderId(reviewDTO.getOrderId()); // Puede ser null
        review.setUserId(userId);
        review.setRestaurantId(reviewDTO.getRestaurantId());
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        
        // Convertir lista de imágenes a JSON string
        if (reviewDTO.getImages() != null && !reviewDTO.getImages().isEmpty()) {
            try {
                review.setImages(objectMapper.writeValueAsString(reviewDTO.getImages()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Error al procesar las imágenes");
            }
        }
        
        Review savedReview = reviewRepository.save(review);
        
        return convertToDTO(savedReview);
    }
    
    /**
     * Obtener reseñas de un restaurante
     */
    public List<ReviewDTO> getReviewsByRestaurantId(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener reseñas de un usuario
     */
    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Verificar si un pedido tiene reseña
     */
    public Map<String, Object> checkOrderHasReview(Long orderId) {
        Map<String, Object> response = new HashMap<>();
        
        if (orderId == null) {
            response.put("hasReview", false);
            response.put("review", null);
            return response;
        }
        
        Optional<Review> review = reviewRepository.findByOrderId(orderId);
        
        response.put("hasReview", review.isPresent());
        
        if (review.isPresent()) {
            response.put("review", convertToDTO(review.get()));
        } else {
            response.put("review", null);
        }
        
        return response;
    }
    
    /**
     * Obtener estadísticas de reseñas de un restaurante
     */
    public ReviewStatisticsDTO getRestaurantStatistics(Long restaurantId) {
        ReviewStatisticsDTO statistics = new ReviewStatisticsDTO();
        
        // Calcular rating promedio
        BigDecimal avgRating = reviewRepository.calculateAverageRatingByRestaurantId(restaurantId);
        if (avgRating != null) {
            statistics.setAverageRating(avgRating.setScale(1, RoundingMode.HALF_UP));
        } else {
            statistics.setAverageRating(BigDecimal.ZERO);
        }
        
        // Contar total de reseñas
        long totalReviews = reviewRepository.countByRestaurantId(restaurantId);
        statistics.setTotalReviews(totalReviews);
        
        // Obtener distribución de ratings
        List<Object[]> distribution = reviewRepository.getRatingDistribution(restaurantId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        
        // Inicializar con 0
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        // Llenar con datos reales
        for (Object[] row : distribution) {
            Integer rating = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            ratingDistribution.put(rating, count);
        }
        
        statistics.setRatingDistribution(ratingDistribution);
        
        // Obtener reseñas recientes (últimas 5)
        List<Review> recentReviews = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        List<ReviewDTO> recentReviewsDTO = recentReviews.stream()
                .limit(5)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        statistics.setRecentReviews(recentReviewsDTO);
        
        return statistics;
    }
    
    /**
     * Obtener una reseña por ID
     */
    public ReviewDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));
        
        return convertToDTO(review);
    }
    
    /**
     * Actualizar una reseña
     */
    @Transactional
    public ReviewDTO updateReview(Long id, ReviewDTO reviewDTO, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));
        
        // Verificar que el usuario sea el dueño de la reseña
        if (!review.getUserId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para editar esta reseña");
        }
        
        // Actualizar campos
        if (reviewDTO.getRating() != null) {
            review.setRating(reviewDTO.getRating());
        }
        if (reviewDTO.getComment() != null) {
            review.setComment(reviewDTO.getComment());
        }
        if (reviewDTO.getImages() != null) {
            try {
                review.setImages(objectMapper.writeValueAsString(reviewDTO.getImages()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Error al procesar las imágenes");
            }
        }
        
        Review updatedReview = reviewRepository.save(review);
        
        return convertToDTO(updatedReview);
    }
    
    /**
     * Eliminar una reseña
     */
    @Transactional
    public void deleteReview(Long id, Long userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con ID: " + id));
        
        // Verificar que el usuario sea el dueño de la reseña
        if (!review.getUserId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para eliminar esta reseña");
        }
        
        reviewRepository.delete(review);
    }
    
    /**
     * Convertir entidad a DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrderId());
        dto.setUserId(review.getUserId());
        dto.setRestaurantId(review.getRestaurantId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        
        // Obtener userName desde la relación User
        if (review.getUser() != null) {
            dto.setUserName(review.getUser().getName());
        }
        
        // Obtener restaurantName desde la relación Restaurant
        if (review.getRestaurant() != null) {
            dto.setRestaurantName(review.getRestaurant().getName());
        }
        
        // Convertir JSON string a lista de imágenes
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(review.getImages(), new TypeReference<List<String>>() {});
                dto.setImages(images);
            } catch (JsonProcessingException e) {
                dto.setImages(new ArrayList<>());
            }
        } else {
            dto.setImages(new ArrayList<>());
        }
        
        return dto;
    }
}
