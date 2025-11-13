package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado
 * Código HTTP: 404 Not Found
 * 
 * Ejemplos:
 * - Usuario no encontrado
 * - Restaurante no encontrado
 * - Producto no encontrado
 * - Orden no encontrada
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private String resourceName;
    private String fieldName;
    private Object fieldValue;
    
    /**
     * Constructor básico con mensaje personalizado
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor con detalles del recurso
     * 
     * @param resourceName Nombre del recurso (ej: "Usuario", "Restaurante")
     * @param fieldName Campo usado para buscar (ej: "id", "email")
     * @param fieldValue Valor del campo buscado
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado con %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public Object getFieldValue() {
        return fieldValue;
    }
}
