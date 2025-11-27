package upeu.edu.pe.restaurant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.BatchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.NotificationDTO;
import upeu.edu.pe.restaurant.entity.FCMToken;
import upeu.edu.pe.restaurant.entity.NotificationLog;
import upeu.edu.pe.restaurant.repository.NotificationLogRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {
    
    private final NotificationLogRepository notificationLogRepository;
    private final FCMTokenService fcmTokenService;
    private final FirebaseMessagingService firebaseMessagingService;
    private final ObjectMapper objectMapper;
    
    public NotificationService(
        NotificationLogRepository notificationLogRepository,
        FCMTokenService fcmTokenService,
        @Autowired(required = false) FirebaseMessagingService firebaseMessagingService,
        ObjectMapper objectMapper
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.fcmTokenService = fcmTokenService;
        this.firebaseMessagingService = firebaseMessagingService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Enviar notificación a un usuario usando Firebase Cloud Messaging
     */
    @Transactional
    public void sendNotification(NotificationDTO notificationDTO) {
        Long userId = notificationDTO.getUserId();
        
        if (userId != null) {
            // Enviar a usuario específico
            sendToUser(userId, notificationDTO);
        } else {
            // TODO: Enviar a todos los usuarios (broadcast)
            log.info("Envío broadcast no implementado aún");
        }
    }
    
    /**
     * Enviar notificación a un usuario específico con FCM
     */
    private void sendToUser(Long userId, NotificationDTO notificationDTO) {
        // Verificar si Firebase está configurado
        if (firebaseMessagingService == null) {
            log.warn("⚠️ Firebase no configurado. No se puede enviar notificación a usuario ID: {}", userId);
            logNotification(userId, notificationDTO);
            return;
        }
        
        // Obtener tokens activos del usuario
        List<FCMToken> tokens = fcmTokenService.getActiveTokensByUserId(userId);
        
        if (tokens.isEmpty()) {
            log.warn("No se encontraron tokens activos para usuario ID: {}", userId);
            return;
        }
        
        // Extraer strings de tokens
        List<String> tokenStrings = tokens.stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());
        
        // Enviar notificación usando Firebase
        BatchResponse response = firebaseMessagingService.sendNotificationToMultipleTokens(
                tokenStrings,
                notificationDTO.getTitle(),
                notificationDTO.getBody(),
                notificationDTO.getData()
        );
        
        // Limpiar tokens inválidos si hay fallos
        if (response != null && response.getFailureCount() > 0) {
            List<String> invalidTokens = firebaseMessagingService.getInvalidTokens(response, tokenStrings);
            cleanupInvalidTokens(invalidTokens);
        }
        
        // Registrar notificación en el log
        logNotification(userId, notificationDTO);
        
        log.info("✅ Notificación enviada a usuario ID: {} (tokens: {}, éxito: {}, fallos: {})", 
                userId, 
                tokens.size(),
                response != null ? response.getSuccessCount() : 0,
                response != null ? response.getFailureCount() : 0);
    }
    
    /**
     * Limpiar tokens inválidos
     */
    @Transactional
    public void cleanupInvalidTokens(List<String> invalidTokens) {
        if (invalidTokens == null || invalidTokens.isEmpty()) {
            return;
        }
        
        for (String token : invalidTokens) {
            fcmTokenService.deactivateToken(token);
        }
        
        log.info("🧹 Limpiados {} tokens inválidos", invalidTokens.size());
    }
    
    /**
     * Registrar notificación en el log
     */
    @Transactional
    public void logNotification(Long userId, NotificationDTO notificationDTO) {
        NotificationLog log = new NotificationLog();
        log.setUserId(userId);
        log.setNotificationType(notificationDTO.getType());
        log.setTitle(notificationDTO.getTitle());
        log.setBody(notificationDTO.getBody());
        
        if (notificationDTO.getData() != null) {
            try {
                log.setData(objectMapper.writeValueAsString(notificationDTO.getData()));
            } catch (JsonProcessingException e) {
                NotificationService.log.error("Error al convertir data a JSON", e);
            }
        }
        
        notificationLogRepository.save(log);
    }
    
    /**
     * Notificar cambio de estado de pedido
     */
    @Transactional
    public void notifyOrderStatusChange(Long userId, Long orderId, String newStatus) {
        if (userId == null || orderId == null || newStatus == null) {
            log.warn("Parámetros inválidos para notificación de pedido");
            return;
        }
        
        // Obtener tokens activos del usuario
        List<FCMToken> tokens = fcmTokenService.getActiveTokensByUserId(userId);
        
        if (tokens.isEmpty()) {
            log.warn("No se encontraron tokens activos para usuario ID: {} (pedido #{})", userId, orderId);
            return;
        }
        
        // Enviar notificación usando Firebase
        firebaseMessagingService.sendOrderStatusNotification(
                tokens,
                orderId,
                newStatus,
                getStatusText(newStatus)
        );
        
        // Registrar en el log
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(userId);
        dto.setType("order_status");
        dto.setTitle(getOrderStatusTitle(newStatus));
        dto.setBody(String.format(getOrderStatusBody(newStatus), orderId));
        dto.setData(Map.of(
                "type", "order_status",
                "id", orderId.toString(),
                "status", newStatus
        ));
        
        logNotification(userId, dto);
        
        log.info("📱 Notificación de pedido enviada a usuario ID: {} (pedido #{}, estado: {})", 
                userId, orderId, newStatus);
    }
    
    /**
     * Notificar oferta especial
     */
    @Transactional
    public void notifySpecialOffer(Long userId, String offerTitle, String offerDescription, String couponId) {
        List<FCMToken> tokens = fcmTokenService.getActiveTokensByUserId(userId);
        
        if (tokens.isEmpty()) {
            log.warn("No se encontraron tokens activos para usuario ID: {}", userId);
            return;
        }
        
        firebaseMessagingService.sendSpecialOfferNotification(
                tokens,
                offerTitle,
                offerDescription,
                couponId
        );
        
        // Registrar en el log
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(userId);
        dto.setType("special_offer");
        dto.setTitle("🎁 " + offerTitle);
        dto.setBody(offerDescription);
        dto.setData(Map.of(
                "type", "special_offer",
                "id", couponId != null ? couponId : ""
        ));
        
        logNotification(userId, dto);
    }
    
    /**
     * Notificar nuevo restaurante
     */
    @Transactional
    public void notifyNewRestaurant(Long userId, Long restaurantId, String restaurantName) {
        List<FCMToken> tokens = fcmTokenService.getActiveTokensByUserId(userId);
        
        if (tokens.isEmpty()) {
            log.warn("No se encontraron tokens activos para usuario ID: {}", userId);
            return;
        }
        
        firebaseMessagingService.sendNewRestaurantNotification(
                tokens,
                restaurantId,
                restaurantName
        );
        
        // Registrar en el log
        NotificationDTO dto = new NotificationDTO();
        dto.setUserId(userId);
        dto.setType("new_restaurant");
        dto.setTitle("🏪 ¡Nuevo restaurante disponible!");
        dto.setBody(String.format("Descubre la deliciosa comida de %s", restaurantName));
        dto.setData(Map.of(
                "type", "new_restaurant",
                "id", restaurantId.toString()
        ));
        
        logNotification(userId, dto);
    }
    
    /**
     * Obtener notificaciones de un usuario
     */
    public List<NotificationLog> getUserNotifications(Long userId) {
        return notificationLogRepository.findByUserIdOrderBySentAtDesc(userId);
    }
    
    /**
     * Obtener notificaciones no leídas
     */
    public List<NotificationLog> getUnreadNotifications(Long userId) {
        return notificationLogRepository.findByUserIdAndReadAtIsNullOrderBySentAtDesc(userId);
    }
    
    /**
     * Marcar notificación como leída
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        NotificationLog notification = notificationLogRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        notification.setReadAt(LocalDateTime.now());
        notificationLogRepository.save(notification);
    }
    
    /**
     * Contar notificaciones no leídas
     */
    public long countUnread(Long userId) {
        return notificationLogRepository.countByUserIdAndReadAtIsNull(userId);
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private String getStatusText(String status) {
        return switch (status.toLowerCase()) {
            case "confirmed" -> "confirmado";
            case "preparing" -> "en preparación";
            case "on_the_way" -> "en camino";
            case "delivered" -> "entregado";
            case "cancelled" -> "cancelado";
            default -> "actualizado";
        };
    }
    
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
