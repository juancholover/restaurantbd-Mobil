package upeu.edu.pe.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.restaurant.entity.FCMToken;

import java.util.List;
import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    
    // Buscar token específico
    Optional<FCMToken> findByToken(String token);
    
    // Obtener todos los tokens activos de un usuario
    List<FCMToken> findByUserIdAndIsActiveTrue(Long userId);
    
    // Obtener todos los tokens de un usuario (activos e inactivos)
    List<FCMToken> findByUserId(Long userId);
    
    // Verificar si existe un token
    boolean existsByToken(String token);
    
    // Eliminar tokens inactivos antiguos (opcional, para limpieza)
    void deleteByIsActiveFalse();
}
