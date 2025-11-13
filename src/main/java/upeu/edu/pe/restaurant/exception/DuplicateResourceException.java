package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe
 * Código HTTP: 409 Conflict
 * 
 * Ejemplo: Registrar un usuario con email ya existente
 */
public class DuplicateResourceException extends RuntimeException {
    private String fieldName;
    private Object fieldValue;
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s ya existe con %s: '%s'", resourceName, fieldName, fieldValue));
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}
