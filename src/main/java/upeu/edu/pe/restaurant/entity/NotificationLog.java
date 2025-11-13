package upeu.edu.pe.restaurant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications_log", indexes = {
    @Index(name = "idx_user_id_notif", columnList = "user_id"),
    @Index(name = "idx_sent_at", columnList = "sent_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @NotBlank(message = "El tipo de notificación es requerido")
    @Column(name = "notification_type", nullable = false)
    private String notificationType; // order_status, special_offer, new_restaurant, general
    
    @NotBlank(message = "El título es requerido")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "El cuerpo es requerido")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;
    
    @Column(columnDefinition = "TEXT")
    private String data; // JSON con datos adicionales
    
    @CreationTimestamp
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
