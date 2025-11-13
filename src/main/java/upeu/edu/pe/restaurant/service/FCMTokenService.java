package upeu.edu.pe.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.FCMTokenDTO;
import upeu.edu.pe.restaurant.entity.FCMToken;
import upeu.edu.pe.restaurant.repository.FCMTokenRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMTokenService {
    
    private final FCMTokenRepository fcmTokenRepository;
    
    /**
     * Registrar o actualizar un token FCM
     */
    @Transactional
    public void registerToken(FCMTokenDTO tokenDTO, Long userId) {
        Optional<FCMToken> existingToken = fcmTokenRepository.findByToken(tokenDTO.getToken());
        
        if (existingToken.isPresent()) {
            // Actualizar token existente
            FCMToken token = existingToken.get();
            token.setUserId(userId);
            token.setDeviceType(tokenDTO.getDeviceType());
            token.setDeviceName(tokenDTO.getDeviceName());
            token.setIsActive(true);
            token.setLastUsedAt(LocalDateTime.now());
            
            fcmTokenRepository.save(token);
            
            log.info("Token FCM actualizado para usuario ID: {}", userId);
        } else {
            // Crear nuevo token
            FCMToken newToken = new FCMToken();
            newToken.setUserId(userId);
            newToken.setToken(tokenDTO.getToken());
            newToken.setDeviceType(tokenDTO.getDeviceType());
            newToken.setDeviceName(tokenDTO.getDeviceName());
            newToken.setIsActive(true);
            newToken.setLastUsedAt(LocalDateTime.now());
            
            fcmTokenRepository.save(newToken);
            
            log.info("Nuevo token FCM registrado para usuario ID: {}", userId);
        }
    }
    
    /**
     * Obtener tokens activos de un usuario
     */
    public List<FCMToken> getActiveTokensByUserId(Long userId) {
        return fcmTokenRepository.findByUserIdAndIsActiveTrue(userId);
    }
    
    /**
     * Desactivar un token (logout)
     */
    @Transactional
    public void deactivateToken(String token) {
        Optional<FCMToken> fcmToken = fcmTokenRepository.findByToken(token);
        
        if (fcmToken.isPresent()) {
            FCMToken tokenEntity = fcmToken.get();
            tokenEntity.setIsActive(false);
            fcmTokenRepository.save(tokenEntity);
            
            log.info("Token FCM desactivado: {}", token);
        }
    }
    
    /**
     * Eliminar un token
     */
    @Transactional
    public void deleteToken(String token) {
        Optional<FCMToken> fcmToken = fcmTokenRepository.findByToken(token);
        
        if (fcmToken.isPresent()) {
            fcmTokenRepository.delete(fcmToken.get());
            log.info("Token FCM eliminado: {}", token);
        }
    }
    
    /**
     * Obtener tokens de un usuario (activos e inactivos)
     */
    public List<FCMToken> getAllTokensByUserId(Long userId) {
        return fcmTokenRepository.findByUserId(userId);
    }
    
    /**
     * Limpiar tokens inactivos antiguos (puede ejecutarse periódicamente)
     */
    @Transactional
    public void cleanupInactiveTokens() {
        fcmTokenRepository.deleteByIsActiveFalse();
        log.info("Tokens inactivos eliminados");
    }
}
