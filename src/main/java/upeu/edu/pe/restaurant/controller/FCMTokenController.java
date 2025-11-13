package upeu.edu.pe.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.FCMTokenDTO;
import upeu.edu.pe.restaurant.security.UserPrincipal;
import upeu.edu.pe.restaurant.service.FCMTokenService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FCMTokenController {
    
    private final FCMTokenService fcmTokenService;
    
    /**
     * POST /api/fcm/token - Registrar token FCM
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> registerToken(
            @Valid @RequestBody FCMTokenDTO tokenDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        fcmTokenService.registerToken(tokenDTO, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Token registrado exitosamente");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/fcm/token - Eliminar token FCM (logout)
     */
    @DeleteMapping("/token")
    public ResponseEntity<Map<String, Object>> deleteToken(
            @RequestBody Map<String, String> request) {
        
        String token = request.get("token");
        fcmTokenService.deleteToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Token eliminado exitosamente");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/fcm/token/deactivate - Desactivar token
     */
    @PostMapping("/token/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateToken(
            @RequestBody Map<String, String> request) {
        
        String token = request.get("token");
        fcmTokenService.deactivateToken(token);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Token desactivado exitosamente");
        
        return ResponseEntity.ok(response);
    }
}
