package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando la solicitud contiene datos inválidos
 * Código HTTP: 400 Bad Request
 * 
 * Ejemplos:
 * - Parámetros faltantes
 * - Formato de datos incorrecto
 * - Lógica de negocio inválida
 */
public class BadRequestException extends RuntimeException {
    
    private String field;
    private Object rejectedValue;
    
    /**
     * Constructor básico con mensaje
     */
    public BadRequestException(String message) {
        super(message);
    }
    
    /**
     * Constructor con causa
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor con detalles del campo inválido
     * 
     * @param field Campo que causó el error
     * @param rejectedValue Valor rechazado
     * @param message Mensaje descriptivo
     */
    public BadRequestException(String field, Object rejectedValue, String message) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
    
    public String getField() {
        return field;
    }
    
    public Object getRejectedValue() {
        return rejectedValue;
    }
}
