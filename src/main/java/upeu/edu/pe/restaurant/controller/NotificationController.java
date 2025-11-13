package upeu.edu.pe.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.NotificationDTO;
import upeu.edu.pe.restaurant.entity.NotificationLog;
import upeu.edu.pe.restaurant.security.UserPrincipal;
import upeu.edu.pe.restaurant.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * POST /api/notifications/send - Enviar notificación (Admin)
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @Valid @RequestBody NotificationDTO notificationDTO) {
        
        notificationService.sendNotification(notificationDTO);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notificación enviada exitosamente");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/notifications/my - Obtener mis notificaciones
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        List<NotificationLog> notifications = notificationService.getUserNotifications(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", notifications);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/notifications/unread - Obtener notificaciones no leídas
     */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        List<NotificationLog> notifications = notificationService.getUnreadNotifications(currentUser.getId());
        long unreadCount = notificationService.countUnread(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", notifications);
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/notifications/{id}/read - Marcar como leída
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notificación marcada como leída");
        
        return ResponseEntity.ok(response);
    }
}
