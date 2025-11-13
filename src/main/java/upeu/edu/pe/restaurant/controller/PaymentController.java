package upeu.edu.pe.restaurant.controller;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.response.ApiResponse;
import upeu.edu.pe.restaurant.dto.PaymentIntentRequest;
import upeu.edu.pe.restaurant.dto.PaymentIntentResponse;
import upeu.edu.pe.restaurant.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Crea un Payment Intent para procesar un pago
     * ✅ REQUIERE AUTENTICACIÓN JWT
     * 
     * POST /api/payments
     * Headers: Authorization: Bearer {JWT_TOKEN}
     * 
     * Body:
     * {
     *   "amount": 5000,        // En centavos: 5000 = $50.00
     *   "currency": "usd",     // Código de moneda
     *   "orderId": 123,        // ID de la orden
     *   "metadata": {          // Datos adicionales opcionales
     *     "customerName": "Juan Pérez"
     *   }
     * }
     */
    @PostMapping
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestBody PaymentIntentRequest request,
            Authentication authentication) {
        try {
            // Obtener email del usuario autenticado
            String userEmail = authentication.getName();
            
            log.info("Solicitud de Payment Intent de usuario: {}, amount={}, currency={}, orderId={}",
                    userEmail, request.getAmount(), request.getCurrency(), request.getOrderId());

            // Validar monto mínimo (50 centavos)
            if (request.getAmount() == null || request.getAmount() < 50) {
                throw new IllegalArgumentException("El monto debe ser al menos 50 centavos");
            }

            PaymentIntentResponse response = paymentService.createPaymentIntent(request);

            log.info("Payment Intent creado exitosamente para usuario: {}", userEmail);
            
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            log.error("Error de Stripe al crear Payment Intent: {}", e.getMessage(), e);
            throw new RuntimeException("Error al procesar el pago: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear Payment Intent: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno del servidor");
        }
    }

    /**
     * Obtiene el estado de un pago
     * 
     * GET /api/payments/status/{paymentIntentId}
     */
    @GetMapping("/status/{paymentIntentId}")
    public ResponseEntity<ApiResponse<String>> getPaymentStatus(
            @PathVariable String paymentIntentId) {
        try {
            String status = paymentService.getPaymentStatus(paymentIntentId);
            return ResponseEntity.ok(
                    ApiResponse.success("Estado del pago obtenido exitosamente", status)
            );
        } catch (StripeException e) {
            log.error("Error al obtener estado del pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener estado del pago: " + e.getMessage()));
        }
    }

    /**
     * Cancela un Payment Intent
     * 
     * POST /api/payments/cancel/{paymentIntentId}
     */
    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<ApiResponse<Boolean>> cancelPaymentIntent(
            @PathVariable String paymentIntentId) {
        try {
            boolean canceled = paymentService.cancelPaymentIntent(paymentIntentId);
            return ResponseEntity.ok(
                    ApiResponse.success("Payment Intent cancelado exitosamente", canceled)
            );
        } catch (StripeException e) {
            log.error("Error al cancelar Payment Intent: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al cancelar el pago: " + e.getMessage()));
        }
    }
}
