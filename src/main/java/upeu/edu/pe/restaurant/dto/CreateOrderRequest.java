package upeu.edu.pe.restaurant.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "El ID del restaurante es requerido")
    private Long restaurantId;
    
    @NotEmpty(message = "Los items de la orden son requeridos")
    private List<OrderItemRequest> items;
    
    @NotNull(message = "El subtotal es requerido")
    private BigDecimal subtotal;
    
    @NotNull(message = "El costo de delivery es requerido")
    private BigDecimal deliveryFee;
    
    @NotNull(message = "El total es requerido")
    private BigDecimal totalAmount;
    
    private String deliveryAddress;
    private String notes;
    
    // ✅ Nuevos campos para pagos con Stripe
    private String paymentIntentId;
    private String paymentMethod;
    private String paymentStatus;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "El ID del producto es requerido")
        private Long productId;
        
        @NotNull(message = "La cantidad es requerida")
        private Integer quantity;
        
        @NotNull(message = "El precio es requerido")
        private BigDecimal price;
    }
}
