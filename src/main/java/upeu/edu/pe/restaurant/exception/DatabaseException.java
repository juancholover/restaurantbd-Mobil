package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando hay errores en operaciones de base de datos
 * Código HTTP: 500 Internal Server Error
 * 
 * Ejemplo: Fallo de conexión a PostgreSQL, constraint violations
 */
public class DatabaseException extends RuntimeException {
    private String operation;
    
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DatabaseException(String operation, String message, Throwable cause) {
        super(String.format("Error en operación '%s': %s", operation, message), cause);
        this.operation = operation;
    }
    
    public String getOperation() {
        return operation;
    }
}
