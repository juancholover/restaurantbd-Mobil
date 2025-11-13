package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando un producto no está disponible
 * Código HTTP: 400 Bad Request
 * 
 * Ejemplo: Producto agotado, producto deshabilitado
 */
public class ProductNotAvailableException extends RuntimeException {
    private Long productId;
    private String productName;
    
    public ProductNotAvailableException(String message) {
        super(message);
    }
    
    public ProductNotAvailableException(Long productId, String productName) {
        super(String.format("El producto '%s' (ID: %d) no está disponible", productName, productId));
        this.productId = productId;
        this.productName = productName;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
}
