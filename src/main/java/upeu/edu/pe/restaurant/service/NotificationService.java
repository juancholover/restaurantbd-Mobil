package upeu.edu.pe.restaurant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.NotificationDTO;
import upeu.edu.pe.restaurant.entity.FCMToken;
import upeu.edu.pe.restaurant.entity.NotificationLog;
import upeu.edu.pe.restaurant.repository.NotificationLogRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationLogRepository notificationLogRepository;
    private final FCMTokenService fcmTokenService;
    private final ObjectMapper objectMapper;
    
    /**
     * Enviar notificación a un usuario
     * NOTA: Implementación básica sin Firebase Admin SDK
     * Para producción, integrar Firebase Admin SDK
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
     * Enviar notificación a un usuario específico
     */
    private void sendToUser(Long userId, NotificationDTO notificationDTO) {
        // Obtener tokens activos del usuario
        List<FCMToken> tokens = fcmTokenService.getActiveTokensByUserId(userId);
        
        if (tokens.isEmpty()) {
            log.warn("No se encontraron tokens activos para usuario ID: {}", userId);
            return;
        }
        
        // TODO: Integrar con Firebase Admin SDK
        // Por ahora solo registramos en el log
        logNotification(userId, notificationDTO);
        
        log.info("Notificación enviada a usuario ID: {} (tokens: {})", userId, tokens.size());
        
        /* Ejemplo de implementación con Firebase Admin SDK:
        
        List<String> tokenStrings = tokens.stream()
                .map(FCMToken::getToken)
                .collect(Collectors.toList());
        
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(notificationDTO.getTitle())
                        .setBody(notificationDTO.getBody())
                        .build())
                .putAllData(notificationDTO.getData())
                .addAllTokens(tokenStrings)
                .build();
        
        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("Notificaciones enviadas: success={}, failure={}", 
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Error al enviar notificación", e);
        }
        */
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
    public void notifyOrderStatusChange(Long userId, Long orderId, String newStatus) {
        String title;
        String body;
        
        switch (newStatus.toLowerCase()) {
            case "confirmed":
                title = "✅ Pedido confirmado";
                body = String.format("Tu pedido #%d ha sido confirmado y está siendo preparado", orderId);
                break;
            case "preparing":
                title = "👨‍🍳 Preparando tu pedido";
                body = String.format("El restaurante está preparando tu pedido #%d", orderId);
                break;
            case "on_the_way":
                title = "🚚 ¡Tu pedido está en camino!";
                body = String.format("El repartidor está en camino con tu pedido #%d", orderId);
                break;
            case "delivered":
                title = "🎉 ¡Pedido entregado!";
                body = String.format("Tu pedido #%d ha sido entregado. ¡Buen provecho!", orderId);
                break;
            case "cancelled":
                title = "❌ Pedido cancelado";
                body = String.format("Tu pedido #%d ha sido cancelado", orderId);
                break;
            default:
                title = "📦 Actualización de pedido";
                body = String.format("Tu pedido #%d ha sido actualizado", orderId);
                break;
        }
        
        NotificationDTO notification = new NotificationDTO();
        notification.setUserId(userId);
        notification.setType("order_status");
        notification.setTitle(title);
        notification.setBody(body);
        notification.setData(java.util.Map.of(
                "orderId", orderId.toString(),
                "status", newStatus
        ));
        
        sendNotification(notification);
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
}
