package upeu.edu.pe.restaurant.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación
 * Captura y formatea errores de forma consistente
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Maneja errores de solicitudes incorrectas (400 Bad Request)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, 
            HttpServletRequest request) {
        
        log.error("❌ Bad Request: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        error.setExceptionType(ex.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja recursos no encontrados (404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, 
            HttpServletRequest request) {
        
        log.error("❌ Resource Not Found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        error.setExceptionType(ex.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Maneja errores de autenticación (401 Unauthorized)
     */
    @ExceptionHandler({BadCredentialsException.class, UnauthorizedException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("❌ Unauthorized: {}", ex.getMessage());
        
        String message = ex instanceof BadCredentialsException 
            ? "Credenciales inválidas. Verifica tu email y contraseña." 
            : ex.getMessage();
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            message,
            request.getRequestURI()
        );
        error.setExceptionType(ex.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Maneja errores de permisos (403 Forbidden)
     */
    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("❌ Forbidden: {}", ex.getMessage());
        
        String message = ex instanceof AccessDeniedException
            ? "No tienes permisos para acceder a este recurso"
            : ex.getMessage();
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            message,
            request.getRequestURI()
        );
        error.setExceptionType(ex.getClass().getSimpleName());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    /**
     * Maneja recursos duplicados (409 Conflict)
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, 
            HttpServletRequest request) {
        
        log.error("❌ Duplicate Resource: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getFieldName() != null) {
            details.put("field", ex.getFieldName());
            details.put("value", ex.getFieldValue());
        }
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getClass().getSimpleName(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Maneja errores de órdenes inválidas (400 Bad Request)
     */
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrder(
            InvalidOrderException ex, 
            HttpServletRequest request) {
        
        log.error("❌ Invalid Order: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getOrderField() != null) {
            details.put("invalidField", ex.getOrderField());
        }
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getClass().getSimpleName(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja productos no disponibles (400 Bad Request)
     */
    @ExceptionHandler(ProductNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleProductNotAvailable(
            ProductNotAvailableException ex, 
            HttpServletRequest request) {
        
        log.error("❌ Product Not Available: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getProductId() != null) {
            details.put("productId", ex.getProductId());
            details.put("productName", ex.getProductName());
        }
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getClass().getSimpleName(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja errores JWT (401 Unauthorized)
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex, 
            HttpServletRequest request) {
        
        log.error("❌ JWT Error: {}", ex.getMessage());
        
        Map<String, Object> details = new HashMap<>();
        if (ex.getTokenType() != null) {
            details.put("tokenType", ex.getTokenType());
        }
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            ex.getClass().getSimpleName(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    /**
     * Maneja errores de base de datos (500 Internal Server Error)
     */
    @ExceptionHandler({DatabaseException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("❌ Database Error: {}", ex.getMessage(), ex);
        
        String message = ex instanceof DatabaseException 
            ? ex.getMessage()
            : "Error de integridad en la base de datos. Verifica las relaciones entre datos.";
        
        Map<String, Object> details = new HashMap<>();
        if (ex instanceof DatabaseException) {
            DatabaseException dbEx = (DatabaseException) ex;
            if (dbEx.getOperation() != null) {
                details.put("operation", dbEx.getOperation());
            }
        }
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            message,
            request.getRequestURI(),
            ex.getClass().getSimpleName(),
            details
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Maneja errores de validación de campos (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, Object> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        log.error("❌ Validation Errors: {}", fieldErrors);
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Errores de validación en los campos enviados",
            request.getRequestURI(),
            ex.getClass().getSimpleName(),
            fieldErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Maneja cualquier otra excepción no capturada (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("❌ Unexpected Error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Ha ocurrido un error inesperado. Por favor contacta al administrador.",
            request.getRequestURI()
        );
        error.setExceptionType(ex.getClass().getSimpleName());
        
        // En desarrollo, incluir stack trace
        if (isDevMode()) {
            error.setStackTrace(getStackTraceAsString(ex));
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Verifica si estamos en modo desarrollo
     */
    private boolean isDevMode() {
        // Puedes configurar esto con un property
        return true; // Cambiar a false en producción
    }
    
    /**
     * Convierte el stack trace a String
     */
    private String getStackTraceAsString(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.toString()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
