package upeu.edu.pe.restaurant.service;

import upeu.edu.pe.restaurant.dto.CouponDTO;
import upeu.edu.pe.restaurant.dto.request.ValidateCouponRequest;
import upeu.edu.pe.restaurant.dto.response.CouponValidationResponse;
import upeu.edu.pe.restaurant.entity.Coupon;
import upeu.edu.pe.restaurant.entity.CouponUsage;
import upeu.edu.pe.restaurant.entity.User;
import upeu.edu.pe.restaurant.exception.BadRequestException;
import upeu.edu.pe.restaurant.exception.ResourceNotFoundException;
import upeu.edu.pe.restaurant.repository.CouponRepository;
import upeu.edu.pe.restaurant.repository.CouponUsageRepository;
import upeu.edu.pe.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de cupones
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
    
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;
    
    /**
     * Validar un cupón para un usuario
     */
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(ValidateCouponRequest request, Long userId) {
        log.info("🎫 Validando cupón: {} para usuario: {}", request.getCode(), userId);
        
        // Buscar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        
        // Buscar cupón por código
        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.getCode())
                .orElse(null);
        
        if (coupon == null) {
            log.warn("❌ Cupón no encontrado: {}", request.getCode());
            return CouponValidationResponse.builder()
                    .valid(false)
                    .code(request.getCode())
                    .message("Cupón no válido")
                    .reason("not_found")
                    .build();
        }
        
        // Validar si el cupón está activo
        if (!coupon.getIsActive()) {
            log.warn("❌ Cupón inactivo: {}", request.getCode());
            return CouponValidationResponse.builder()
                    .valid(false)
                    .code(coupon.getCode())
                    .message("Este cupón ya no está disponible")
                    .reason("inactive")
                    .build();
        }
        
        // Validar si el cupón expiró
        if (coupon.isExpired()) {
            log.warn("❌ Cupón expirado: {}", request.getCode());
            return CouponValidationResponse.builder()
                    .valid(false)
                    .code(coupon.getCode())
                    .message("Este cupón ha expirado")
                    .expiresAt(coupon.getExpiresAt())
                    .reason("expired")
                    .build();
        }
        
        // Validar límite de usos global
        if (coupon.hasReachedUsageLimit()) {
            log.warn("❌ Cupón alcanzó límite de usos: {}", request.getCode());
            return CouponValidationResponse.builder()
                    .valid(false)
                    .code(coupon.getCode())
                    .message("Este cupón ya no está disponible")
                    .reason("usage_limit_reached")
                    .build();
        }
        
        // Validar límite de usos por usuario
        if (coupon.getUserUsageLimit() != null) {
            long userUsageCount = couponUsageRepository.countByUserAndCoupon(user, coupon);
            if (userUsageCount >= coupon.getUserUsageLimit()) {
                log.warn("❌ Usuario alcanzó límite de usos del cupón: {}", request.getCode());
                return CouponValidationResponse.builder()
                        .valid(false)
                        .code(coupon.getCode())
                        .message("Ya has utilizado este cupón el máximo de veces permitido")
                        .reason("user_usage_limit_reached")
                        .build();
            }
        }
        
        // Validar monto mínimo
        BigDecimal minimumAmount = coupon.getMinimumAmount();
        if (request.getCartTotal().compareTo(minimumAmount) < 0) {
            log.warn("❌ Monto mínimo no alcanzado: {} < {}", 
                    request.getCartTotal(), minimumAmount);
            return CouponValidationResponse.builder()
                    .valid(false)
                    .code(coupon.getCode())
                    .message(String.format("Monto mínimo de compra: S/%.2f", minimumAmount))
                    .reason("minimum_amount_not_met")
                    .build();
        }
        
        // Calcular descuento
        BigDecimal discount = coupon.calculateDiscount(request.getCartTotal());
        BigDecimal finalTotal = request.getCartTotal().subtract(discount);
        
        log.info("✅ Cupón válido: {} - Descuento: S/{}", coupon.getCode(), discount);
        
        return CouponValidationResponse.builder()
                .valid(true)
                .code(coupon.getCode())
                .message("Cupón aplicado correctamente")
                .discount(discount)
                .discountType(coupon.getDiscountType().toString().toLowerCase())
                .discountValue(coupon.getDiscountValue())
                .finalTotal(finalTotal)
                .expiresAt(coupon.getExpiresAt())
                .build();
    }
    
    /**
     * Registrar el uso de un cupón
     */
    @Transactional
    public void useCoupon(String code, Long userId, Long orderId) {
        log.info("📝 Registrando uso de cupón: {} por usuario: {}", code, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón", "code", code));
        
        // Incrementar contador de usos
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);
        
        // Registrar uso
        CouponUsage usage = CouponUsage.builder()
                .user(user)
                .coupon(coupon)
                .order(null) // Se puede actualizar después con la orden
                .build();
        
        couponUsageRepository.save(usage);
        log.info("✅ Uso de cupón registrado correctamente");
    }
    
    /**
     * Obtener historial de cupones de un usuario
     */
    @Transactional(readOnly = true)
    public List<CouponDTO> getUserCouponHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
        
        List<CouponUsage> usages = couponUsageRepository.findByUserOrderByUsedAtDesc(user);
        
        return usages.stream()
                .map(usage -> convertToDTO(usage.getCoupon()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener todos los cupones activos
     */
    @Transactional(readOnly = true)
    public List<CouponDTO> getActiveCoupons() {
        List<Coupon> coupons = couponRepository.findActiveCoupons(LocalDateTime.now());
        return coupons.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crear un nuevo cupón (admin)
     */
    @Transactional
    public CouponDTO createCoupon(CouponDTO dto) {
        // Validar código único
        if (couponRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BadRequestException("El código del cupón ya existe: " + dto.getCode());
        }
        
        Coupon coupon = Coupon.builder()
                .code(dto.getCode().toUpperCase())
                .description(dto.getDescription())
                .discountType(Coupon.DiscountType.valueOf(dto.getDiscountType()))
                .discountValue(dto.getDiscountValue())
                .minimumAmount(dto.getMinimumAmount())
                .maximumDiscount(dto.getMaximumDiscount())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .expiresAt(dto.getExpiresAt())
                .usageLimit(dto.getUsageLimit())
                .usageCount(0)
                .userUsageLimit(dto.getUserUsageLimit())
                .build();
        
        Coupon saved = couponRepository.save(coupon);
        log.info("✅ Cupón creado: {}", saved.getCode());
        
        return convertToDTO(saved);
    }
    
    /**
     * Activar/Desactivar un cupón (admin)
     */
    @Transactional
    public CouponDTO toggleCouponStatus(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón", "id", couponId));
        
        // Alternar el estado
        coupon.setIsActive(!coupon.getIsActive());
        Coupon updated = couponRepository.save(coupon);
        
        log.info("✅ Estado del cupón {} actualizado a: {}", 
                coupon.getCode(), updated.getIsActive() ? "ACTIVO" : "INACTIVO");
        
        return convertToDTO(updated);
    }
    
    /**
     * Convertir entidad a DTO
     */
    private CouponDTO convertToDTO(Coupon coupon) {
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType().toString())
                .discountValue(coupon.getDiscountValue())
                .minimumAmount(coupon.getMinimumAmount())
                .maximumDiscount(coupon.getMaximumDiscount())
                .isActive(coupon.getIsActive())
                .expiresAt(coupon.getExpiresAt())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .userUsageLimit(coupon.getUserUsageLimit())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
