package upeu.edu.pe.restaurant.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PaymentIntentRequest {
    private Long amount; // Monto en centavos (ej: 1000 = $10.00)
    private String currency; // Código de moneda (usd, pen, etc.)
    private Long orderId; // ID de la orden
    private Map<String, String> metadata; // Metadatos adicionales
}
