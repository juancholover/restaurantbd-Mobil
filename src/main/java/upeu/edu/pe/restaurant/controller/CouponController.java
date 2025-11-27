package upeu.edu.pe.restaurant.controller;

import jakarta.validation.Valid;
import upeu.edu.pe.restaurant.dto.CouponDTO;
import upeu.edu.pe.restaurant.dto.request.ValidateCouponRequest;
import upeu.edu.pe.restaurant.dto.response.ApiResponse;
import upeu.edu.pe.restaurant.dto.response.CouponValidationResponse;
import upeu.edu.pe.restaurant.security.UserPrincipal;
import upeu.edu.pe.restaurant.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de cupones
 */
@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    /**
     * Validar un cupón
     * POST /api/coupons/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> validateCoupon(
            @Valid @RequestBody ValidateCouponRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("📥 POST /api/coupons/validate - Usuario: {}, Código: {}", 
                currentUser.getId(), request.getCode());
        
        CouponValidationResponse response = couponService.validateCoupon(
                request, 
                currentUser.getId()
        );
        
        if (response.getValid()) {
            return ResponseEntity.ok(
                    ApiResponse.success(
                            response.getMessage(), 
                            response
                    )
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.<CouponValidationResponse>builder()
                            .success(false)
                            .message(response.getMessage())
                            .data(response)
                            .build()
            );
        }
    }
    
    /**
     * Obtener historial de cupones del usuario
     * GET /api/coupons/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getCouponHistory(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        log.info("📥 GET /api/coupons/history - Usuario: {}", currentUser.getId());
        
        List<CouponDTO> history = couponService.getUserCouponHistory(currentUser.getId());
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Historial de cupones obtenido", 
                        history
                )
        );
    }
    
    /**
     * Obtener cupones activos (públicos)
     * GET /api/coupons/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CouponDTO>>> getActiveCoupons() {
        log.info("📥 GET /api/coupons/active");
        
        List<CouponDTO> coupons = couponService.getActiveCoupons();
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cupones activos obtenidos", 
                        coupons
                )
        );
    }
    
    /**
     * Crear un nuevo cupón (SOLO ADMIN)
     * POST /api/coupons
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(
            @Valid @RequestBody CouponDTO dto) {
        
        log.info("📥 POST /api/coupons - Creando cupón: {}", dto.getCode());
        
        CouponDTO created = couponService.createCoupon(dto);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cupón creado exitosamente", 
                        created
                )
        );
    }
    
    /**
     * Activar/Desactivar un cupón (SOLO ADMIN)
     * PUT /api/coupons/{id}/toggle
     */
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponDTO>> toggleCoupon(@PathVariable Long id) {
        log.info("📥 PUT /api/coupons/{}/toggle - Alternando estado del cupón", id);
        
        CouponDTO toggled = couponService.toggleCouponStatus(id);
        
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Estado del cupón actualizado", 
                        toggled
                )
        );
    }
}
