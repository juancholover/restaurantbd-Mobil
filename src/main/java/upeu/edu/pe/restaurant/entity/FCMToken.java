package upeu.edu.pe.restaurant.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_tokens", indexes = {
    @Index(name = "idx_user_id_fcm", columnList = "user_id"),
    @Index(name = "idx_is_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "unique_token", columnNames = {"token"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FCMToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "El usuario es requerido")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @NotBlank(message = "El token es requerido")
    @Column(nullable = false, length = 500)
    private String token;
    
    @NotBlank(message = "El tipo de dispositivo es requerido")
    @Column(name = "device_type", nullable = false)
    private String deviceType; // android, ios, web
    
    @Column(name = "device_name")
    private String deviceName;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastUsedAt == null) {
            lastUsedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }
}
