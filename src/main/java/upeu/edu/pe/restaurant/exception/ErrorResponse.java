package upeu.edu.pe.restaurant.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Clase para formatear respuestas de error de manera consistente
 * Proporciona información detallada sobre el error para debugging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Timestamp del error
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * Código de estado HTTP
     */
    private int status;
    
    /**
     * Nombre del estado HTTP (ej: "BAD_REQUEST")
     */
    private String error;
    
    /**
     * Mensaje descriptivo del error
     */
    private String message;
    
    /**
     * Path del endpoint donde ocurrió el error
     */
    private String path;
    
    /**
     * Tipo de excepción que causó el error
     */
    private String exceptionType;
    
    /**
     * Detalles adicionales del error (campos inválidos, etc.)
     */
    private Map<String, Object> details;
    
    /**
     * Stack trace (solo en modo desarrollo)
     */
    private String stackTrace;
    
    /**
     * Crear una respuesta de error básica
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
    
    /**
     * Crear una respuesta de error con detalles adicionales
     */
    public static ErrorResponse of(int status, String error, String message, String path, 
                                   String exceptionType, Map<String, Object> details) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .exceptionType(exceptionType)
                .details(details)
                .build();
    }
}
