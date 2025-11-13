package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando un usuario no está autorizado para realizar una acción
 * Código HTTP: 401 Unauthorized
 * 
 * Ejemplo: Token JWT inválido o expirado
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
