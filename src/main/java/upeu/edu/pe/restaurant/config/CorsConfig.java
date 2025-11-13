package upeu.edu.pe.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing)
 * Permite que la aplicación Flutter web acceda al backend desde diferentes orígenes
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ Permitir todos los orígenes (desarrollo)
        // Para producción, especifica los dominios exactos: Arrays.asList("https://miapp.com")
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // ✅ Permitir todos los métodos HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // ✅ Permitir todos los headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // ✅ Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // ✅ Headers que el cliente puede leer
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Total-Count"
        ));
        
        // ✅ Cache de preflight request (OPTIONS) por 1 hora
        configuration.setMaxAge(3600L);
        
        // ✅ Aplicar configuración a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
