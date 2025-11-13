package upeu.edu.pe.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private BigDecimal rating;
    private Boolean isAvailable;
    private List<ProductOptionDTO> options;
}
