package upeu.edu.pe.restaurant.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import upeu.edu.pe.restaurant.entity.FCMToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FirebaseMessagingService {

    private final FirebaseApp firebaseApp;
    
    public FirebaseMessagingService(@Autowired(required = false) FirebaseApp firebaseApp) {
        this.firebaseApp = firebaseApp;
        if (firebaseApp == null) {
            log.warn("⚠️ FirebaseApp no disponible. Las notificaciones push no funcionarán.");
        }
    }

    /**
     * Enviar notificación a un solo token
     */
    public void sendNotificationToToken(String token, String title, String body, Map<String, String> data) {
        if (!isFirebaseInitialized()) {
            log.warn("Firebase not initialized. Skipping notification to token: {}", token);
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setChannelId("high_importance_channel")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .setBadge(1)
                                    .build())
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance(firebaseApp).send(messageBuilder.build());
            log.info("✅ Notification sent successfully: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("❌ Error sending notification: {}", e.getMessage());
            handleMessagingError(e, token);
        }
    }

    /**
     * Enviar notificación a múltiples tokens (batch)
     */
    public BatchResponse sendNotificationToMultipleTokens(
            List<String> tokens, 
            String title, 
            String body, 
            Map<String, String> data) {
        
        if (!isFirebaseInitialized()) {
            log.warn("Firebase not initialized. Skipping notification to {} tokens", tokens.size());
            return null;
        }

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens provided for notification");
            return null;
        }

        // Firebase limita a 500 tokens por request
        if (tokens.size() > 500) {
            log.warn("Token list exceeds 500. Only first 500 will be used.");
            tokens = tokens.subList(0, 500);
        }

        try {
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setChannelId("high_importance_channel")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .setBadge(1)
                                    .build())
                            .build())
                    .addAllTokens(tokens);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            BatchResponse response = FirebaseMessaging.getInstance(firebaseApp)
                    .sendMulticast(messageBuilder.build());

            log.info("✅ Batch notification sent: {} success, {} failure out of {}",
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    tokens.size());

            // Procesar tokens fallidos
            if (response.getFailureCount() > 0) {
                handleFailedTokens(response, tokens);
            }

            return response;

        } catch (FirebaseMessagingException e) {
            log.error("❌ Error sending batch notification: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Enviar notificación de cambio de estado de pedido
     */
    public void sendOrderStatusNotification(
            List<FCMToken> tokens,
            Long orderId,
            String status,
            String statusText) {

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens available for order status notification");
            return;
        }

        String title = getOrderStatusTitle(status);
        String body = String.format(getOrderStatusBody(status), orderId);

        Map<String, String> data = Map.of(
                "type", "order_status",
                "id", orderId.toString(),
                "status", status,
                "click_action", "FLUTTER_NOTIFICATION_CLICK"
        );

        List<String> tokenStrings = tokens.stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());

        sendNotificationToMultipleTokens(tokenStrings, title, body, data);
    }

    /**
     * Enviar notificación de oferta especial
     */
    public void sendSpecialOfferNotification(
            List<FCMToken> tokens,
            String offerTitle,
            String offerDescription,
            String couponId) {

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens available for special offer notification");
            return;
        }

        Map<String, String> data = Map.of(
                "type", "special_offer",
                "id", couponId != null ? couponId : "",
                "click_action", "FLUTTER_NOTIFICATION_CLICK"
        );

        List<String> tokenStrings = tokens.stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());

        sendNotificationToMultipleTokens(tokenStrings, "🎁 " + offerTitle, offerDescription, data);
    }

    /**
     * Enviar notificación de nuevo restaurante
     */
    public void sendNewRestaurantNotification(
            List<FCMToken> tokens,
            Long restaurantId,
            String restaurantName) {

        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens available for new restaurant notification");
            return;
        }

        String title = "🏪 ¡Nuevo restaurante disponible!";
        String body = String.format("Descubre la deliciosa comida de %s", restaurantName);

        Map<String, String> data = Map.of(
                "type", "new_restaurant",
                "id", restaurantId.toString(),
                "click_action", "FLUTTER_NOTIFICATION_CLICK"
        );

        List<String> tokenStrings = tokens.stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());

        sendNotificationToMultipleTokens(tokenStrings, title, body, data);
    }

    /**
     * Obtener tokens inválidos de una respuesta batch
     */
    public List<String> getInvalidTokens(BatchResponse response, List<String> originalTokens) {
        List<String> invalidTokens = new ArrayList<>();

        if (response == null || response.getResponses() == null) {
            return invalidTokens;
        }

        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (!sendResponse.isSuccessful() && i < originalTokens.size()) {
                FirebaseMessagingException exception = sendResponse.getException();
                if (exception != null && isInvalidTokenError(exception)) {
                    invalidTokens.add(originalTokens.get(i));
                }
            }
        }

        return invalidTokens;
    }

    /**
     * Verificar si Firebase está inicializado
     */
    private boolean isFirebaseInitialized() {
        return firebaseApp != null;
    }

    /**
     * Manejar tokens fallidos
     */
    private void handleFailedTokens(BatchResponse response, List<String> tokens) {
        List<String> invalidTokens = getInvalidTokens(response, tokens);

        if (!invalidTokens.isEmpty()) {
            log.warn("Found {} invalid tokens that should be cleaned up", invalidTokens.size());
            // Los tokens inválidos deberían ser marcados como inactivos en la base de datos
            // Esto se manejará desde NotificationService
        }
    }

    /**
     * Manejar errores de mensajería
     */
    private void handleMessagingError(FirebaseMessagingException e, String token) {
        String errorCode = e.getErrorCode().name();

        if (isInvalidTokenError(e)) {
            log.warn("Invalid token detected: {}. Should be deactivated.", token);
        } else {
            log.error("Messaging error [{}]: {}", errorCode, e.getMessage());
        }
    }

    /**
     * Verificar si es un error de token inválido
     */
    private boolean isInvalidTokenError(FirebaseMessagingException e) {
        String errorCode = e.getErrorCode().name();
        return errorCode.equals("INVALID_ARGUMENT") ||
                errorCode.equals("REGISTRATION_TOKEN_NOT_REGISTERED") ||
                errorCode.equals("INVALID_REGISTRATION_TOKEN");
    }

    /**
     * Obtener título según estado del pedido
     */
    private String getOrderStatusTitle(String status) {
        return switch (status.toLowerCase()) {
            case "confirmed" -> "✅ Pedido confirmado";
            case "preparing" -> "👨‍🍳 Preparando tu pedido";
            case "on_the_way" -> "🚚 ¡Tu pedido está en camino!";
            case "delivered" -> "🎉 ¡Pedido entregado!";
            case "cancelled" -> "❌ Pedido cancelado";
            default -> "📦 Actualización de pedido";
        };
    }

    /**
     * Obtener cuerpo del mensaje según estado del pedido
     */
    private String getOrderStatusBody(String status) {
        return switch (status.toLowerCase()) {
            case "confirmed" -> "Tu pedido #%d ha sido confirmado y está siendo preparado";
            case "preparing" -> "El restaurante está preparando tu pedido #%d";
            case "on_the_way" -> "El repartidor está en camino con tu pedido #%d";
            case "delivered" -> "Tu pedido #%d ha sido entregado. ¡Buen provecho!";
            case "cancelled" -> "Tu pedido #%d ha sido cancelado";
            default -> "Tu pedido #%d ha sido actualizado";
        };
    }
}
