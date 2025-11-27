package upeu.edu.pe.restaurant.controller;

import upeu.edu.pe.restaurant.dto.UpdateProfileDTO;
import upeu.edu.pe.restaurant.dto.request.LoginRequest;
import upeu.edu.pe.restaurant.dto.request.RegisterRequest;
import upeu.edu.pe.restaurant.dto.response.ApiResponse;
import upeu.edu.pe.restaurant.dto.response.AuthResponse;
import upeu.edu.pe.restaurant.dto.response.UserResponse;
import upeu.edu.pe.restaurant.entity.User;
import upeu.edu.pe.restaurant.repository.UserRepository;
import upeu.edu.pe.restaurant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", 
                        UserResponse.fromUser(user))
        );
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileDTO request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Actualizar nombre y teléfono
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        
        // Guardar cambios
        User updatedUser = userRepository.save(user);
        
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", 
                        UserResponse.fromUser(updatedUser))
        );
    }
}
