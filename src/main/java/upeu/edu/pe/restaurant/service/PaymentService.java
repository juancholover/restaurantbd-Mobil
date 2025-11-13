package upeu.edu.pe.restaurant.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import upeu.edu.pe.restaurant.dto.PaymentIntentRequest;
import upeu.edu.pe.restaurant.dto.PaymentIntentResponse;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe API inicializada");
    }

    /**
     * Crea un Payment Intent en Stripe
     * 
     * @param request Datos del pago (monto, moneda, orden)
     * @return Respuesta con clientSecret para completar el pago en el frontend
     * @throws StripeException Si hay un error con Stripe
     */
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException {
        try {
            log.info("Creando Payment Intent para orden: {}, monto: {}", 
                    request.getOrderId(), request.getAmount());

            // Construir metadata
            Map<String, String> metadata = new HashMap<>();
            if (request.getOrderId() != null) {
                metadata.put("orderId", request.getOrderId().toString());
            }
            if (request.getMetadata() != null) {
                metadata.putAll(request.getMetadata());
            }

            // Crear parámetros del Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount())
                    .setCurrency(request.getCurrency() != null ? request.getCurrency() : "usd")
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            // Crear el Payment Intent
            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Payment Intent creado exitosamente: {}", intent.getId());

            return new PaymentIntentResponse(
                    intent.getClientSecret(),
                    intent.getId(),
                    intent.getAmount(),
                    intent.getCurrency()
            );

        } catch (StripeException e) {
            log.error("Error al crear Payment Intent: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Obtiene el estado de un Payment Intent
     * 
     * @param paymentIntentId ID del Payment Intent
     * @return Estado del pago
     * @throws StripeException Si hay un error con Stripe
     */
    public String getPaymentStatus(String paymentIntentId) throws StripeException {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return intent.getStatus();
        } catch (StripeException e) {
            log.error("Error al obtener estado del pago: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cancela un Payment Intent
     * 
     * @param paymentIntentId ID del Payment Intent a cancelar
     * @return true si se canceló exitosamente
     * @throws StripeException Si hay un error con Stripe
     */
    public boolean cancelPaymentIntent(String paymentIntentId) throws StripeException {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent = intent.cancel();
            log.info("Payment Intent cancelado: {}", paymentIntentId);
            return "canceled".equals(intent.getStatus());
        } catch (StripeException e) {
            log.error("Error al cancelar Payment Intent: {}", e.getMessage(), e);
            throw e;
        }
    }
}
