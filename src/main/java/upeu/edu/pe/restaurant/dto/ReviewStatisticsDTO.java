package upeu.edu.pe.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatisticsDTO {
    
    private BigDecimal averageRating;
    
    private Long totalReviews;
    
    private Map<Integer, Long> ratingDistribution; // ej: {5: 20, 4: 15, 3: 7, 2: 2, 1: 1}
    
    private List<ReviewDTO> recentReviews;
}
