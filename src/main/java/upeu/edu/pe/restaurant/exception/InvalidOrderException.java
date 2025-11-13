package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando una orden tiene datos inválidos
 * Código HTTP: 400 Bad Request
 * 
 * Ejemplo: Orden sin items, productos no disponibles, restaurante cerrado
 */
public class InvalidOrderException extends RuntimeException {
    private String orderField;
    
    public InvalidOrderException(String message) {
        super(message);
    }
    
    public InvalidOrderException(String orderField, String message) {
        super(message);
        this.orderField = orderField;
    }
    
    public String getOrderField() {
        return orderField;
    }
}
