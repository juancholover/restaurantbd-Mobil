package upeu.edu.pe.restaurant.config;

import upeu.edu.pe.restaurant.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Habilitar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // ✅ Deshabilitar CSRF (no necesario para API REST stateless)
                .csrf(csrf -> csrf.disable())
                // ✅ Configurar sesiones como STATELESS (sin sesiones)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // ✅ Configurar autorización de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/auth/**").permitAll()           // Login, Register
                        .requestMatchers("/api/restaurants/**").permitAll()    // Listar/Buscar restaurantes
                        .requestMatchers("/api/products/**").permitAll()       // Listar/Buscar productos
                        .requestMatchers("/api/coupons/active").permitAll()    // Cupones activos (público)
                        .requestMatchers("/api/reviews/restaurant/**").permitAll() // Reseñas de restaurante (público)
                        .requestMatchers("/health").permitAll()                // Health check
                        .requestMatchers("/error").permitAll()                 // Manejo de errores
                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                // ✅ Proveedor de autenticación
                .authenticationProvider(authenticationProvider())
                // ✅ Filtro JWT antes de UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, 
                        UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
