package upeu.edu.pe.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.UserAddressDTO;
import upeu.edu.pe.restaurant.entity.User;
import upeu.edu.pe.restaurant.repository.UserRepository;
import upeu.edu.pe.restaurant.service.UserAddressService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserAddressController {
    
    private final UserAddressService addressService;
    private final UserRepository userRepository;
    
   
    @GetMapping
    public ResponseEntity<?> getUserAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            List<UserAddressDTO> addresses = addressService.getUserAddresses(user.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", addresses
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
 
    @PostMapping
    public ResponseEntity<?> createAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserAddressDTO addressDTO) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            UserAddressDTO newAddress = addressService.createAddress(user.getId(), addressDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dirección guardada exitosamente",
                "data", newAddress
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
  
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserAddressDTO addressDTO) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            UserAddressDTO updatedAddress = addressService.updateAddress(user.getId(), id, addressDTO);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dirección actualizada exitosamente",
                "data", updatedAddress
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
   
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            addressService.deleteAddress(user.getId(), id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dirección eliminada exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
