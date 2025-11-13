package upeu.edu.pe.restaurant.exception;

/**
 * Excepción lanzada cuando hay problemas con tokens JWT
 * Código HTTP: 401 Unauthorized
 * 
 * Ejemplo: Token expirado, token malformado, firma inválida
 */
public class JwtException extends RuntimeException {
    private String tokenType;
    
    public JwtException(String message) {
        super(message);
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public JwtException(String tokenType, String message) {
        super(message);
        this.tokenType = tokenType;
    }
    
    public String getTokenType() {
        return tokenType;
    }
}
