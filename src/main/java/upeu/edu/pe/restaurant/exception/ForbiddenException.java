package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando un usuario autenticado no tiene permisos para una acción
 * Código HTTP: 403 Forbidden
 * 
 * Ejemplo: Usuario normal intentando acceder a endpoints de admin
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
