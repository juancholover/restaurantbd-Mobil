package upeu.edu.pe.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.restaurant.entity.NotificationLog;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    
    // Obtener notificaciones de un usuario
    List<NotificationLog> findByUserIdOrderBySentAtDesc(Long userId);
    
    // Obtener notificaciones no leídas de un usuario
    List<NotificationLog> findByUserIdAndReadAtIsNullOrderBySentAtDesc(Long userId);
    
    // Contar notificaciones no leídas
    long countByUserIdAndReadAtIsNull(Long userId);
    
    // Obtener notificaciones por tipo
    List<NotificationLog> findByNotificationTypeOrderBySentAtDesc(String notificationType);
}
