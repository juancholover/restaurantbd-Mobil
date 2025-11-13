package upeu.edu.pe.restaurant.exception;

/**
 * EJEMPLOS PRÁCTICOS DE USO DE EXCEPCIONES
 * 
 * Este archivo contiene ejemplos de cómo usar correctamente
 * el sistema de excepciones en servicios y controllers.
 * 
 * ⚠️ NOTA: Este archivo es solo de referencia y no debe compilarse.
 * Copia los ejemplos a tus servicios según necesites.
 */

/*
// ========================================
// EJEMPLO 1: AuthService - Registro de Usuario
// ========================================

@Service
public class AuthService {
    
    public AuthResponse register(RegisterRequest request) {
        // ✅ Validar email duplicado
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "Usuario", 
                "email", 
                request.getEmail()
            );
        }
        
        // ✅ Validar username duplicado
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                "Usuario", 
                "username", 
                request.getUsername()
            );
        }
        
        // ... continuar con el registro
    }
}


// ========================================
// EJEMPLO 2: RestaurantService - Buscar por ID
// ========================================

@Service
public class RestaurantService {
    
    public RestaurantDTO getRestaurantById(Long id) {
        // ✅ Usar forma detallada de ResourceNotFoundException
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurante", 
                "id", 
                id
            ));
        
        return convertToDTO(restaurant);
    }
    
    public RestaurantDTO updateRestaurant(Long id, RestaurantDTO dto) {
        // ✅ Validar existencia
        Restaurant restaurant = restaurantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurante", 
                "id", 
                id
            ));
        
        // ✅ Validar nombre duplicado (si cambió el nombre)
        if (!restaurant.getName().equals(dto.getName())) {
            if (restaurantRepository.existsByName(dto.getName())) {
                throw new DuplicateResourceException(
                    "Restaurante", 
                    "nombre", 
                    dto.getName()
                );
            }
        }
        
        // ... actualizar restaurante
    }
}


// ========================================
// EJEMPLO 3: ProductService - Validar Disponibilidad
// ========================================

@Service
public class ProductService {
    
    public ProductDTO getProductById(Long id) {
        // ✅ Buscar producto
        MenuItem product = menuItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Producto", 
                "id", 
                id
            ));
        
        return convertToDTO(product);
    }
    
    public void validateProductAvailability(Long productId) {
        MenuItem product = menuItemRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Producto", 
                "id", 
                productId
            ));
        
        // ✅ Validar disponibilidad
        if (!product.isAvailable()) {
            throw new ProductNotAvailableException(
                product.getId(), 
                product.getName()
            );
        }
    }
    
    public ProductDTO createProduct(ProductDTO dto) {
        // ✅ Validar restaurante existe
        Restaurant restaurant = restaurantRepository.findById(dto.getRestaurantId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurante", 
                "id", 
                dto.getRestaurantId()
            ));
        
        // ✅ Validar precio válido
        if (dto.getPrice() <= 0) {
            throw new BadRequestException(
                "price", 
                dto.getPrice(), 
                "El precio debe ser mayor a 0"
            );
        }
        
        // ... crear producto
    }
}


// ========================================
// EJEMPLO 4: OrderService - Crear Orden
// ========================================

@Service
public class OrderService {
    
    public OrderDTO createOrder(CreateOrderRequest request, Long userId) {
        // ✅ Validar usuario existe
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Usuario", 
                "id", 
                userId
            ));
        
        // ✅ Validar que la orden tenga items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException(
                "items", 
                "La orden debe tener al menos un producto"
            );
        }
        
        // ✅ Validar restaurante existe
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Restaurante", 
                "id", 
                request.getRestaurantId()
            ));
        
        // ✅ Validar cada producto
        for (var item : request.getItems()) {
            MenuItem product = menuItemRepository.findById(item.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Producto", 
                    "id", 
                    item.getProductId()
                ));
            
            // ✅ Validar disponibilidad
            if (!product.isAvailable()) {
                throw new ProductNotAvailableException(
                    product.getId(), 
                    product.getName()
                );
            }
            
            // ✅ Validar que el producto pertenece al restaurante
            if (!product.getRestaurant().getId().equals(request.getRestaurantId())) {
                throw new InvalidOrderException(
                    "products", 
                    "Todos los productos deben ser del mismo restaurante"
                );
            }
            
            // ✅ Validar cantidad
            if (item.getQuantity() <= 0) {
                throw new BadRequestException(
                    "quantity", 
                    item.getQuantity(), 
                    "La cantidad debe ser mayor a 0"
                );
            }
        }
        
        // ... crear orden
    }
    
    public OrderDTO getOrderById(Long orderId, Long userId) {
        // ✅ Buscar orden
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Orden", 
                "id", 
                orderId
            ));
        
        // ✅ Validar permisos (solo el dueño puede ver la orden)
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException(
                "No tienes permisos para ver esta orden"
            );
        }
        
        return convertToDTO(order);
    }
    
    public OrderDTO updateOrderStatus(Long orderId, Order.Status newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Orden", 
                "id", 
                orderId
            ));
        
        // ✅ Validar transición de estado válida
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new BadRequestException(
                "status", 
                newStatus, 
                String.format(
                    "No se puede cambiar de %s a %s", 
                    order.getStatus(), 
                    newStatus
                )
            );
        }
        
        order.setStatus(newStatus);
        orderRepository.save(order);
        return convertToDTO(order);
    }
    
    private boolean isValidStatusTransition(Order.Status from, Order.Status to) {
        // Lógica de validación de transiciones
        return true;
    }
}


// ========================================
// EJEMPLO 5: JWT Authentication Filter
// ========================================

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // ✅ Validar JWT
            if (!jwtTokenProvider.validateToken(jwt)) {
                throw new JwtException("Token JWT inválido o expirado");
            }
            
            // ... continuar autenticación
            
        } catch (JwtException ex) {
            // ✅ El GlobalExceptionHandler capturará esto
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Error al procesar token JWT", ex);
        }
        
        filterChain.doFilter(request, response);
    }
}


// ========================================
// EJEMPLO 6: Controller con Validaciones
// ========================================

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantDTO>> getRestaurantById(
            @PathVariable Long id) {
        
        // ✅ El servicio lanzará ResourceNotFoundException si no existe
        RestaurantDTO restaurant = restaurantService.getRestaurantById(id);
        
        return ResponseEntity.ok(ApiResponse.success(restaurant));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // ✅ Verificar permisos de admin
        if (!userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new ForbiddenException(
                "Solo administradores pueden eliminar restaurantes"
            );
        }
        
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Restaurante eliminado"));
    }
}


// ========================================
// EJEMPLO 7: Manejo de Errores de Base de Datos
// ========================================

@Service
public class OrderService {
    
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request, Long userId) {
        try {
            // ... lógica de creación de orden
            
            Order savedOrder = orderRepository.save(order);
            return convertToDTO(savedOrder);
            
        } catch (DataIntegrityViolationException ex) {
            // ✅ Capturar errores de BD
            throw new DatabaseException(
                "CREATE_ORDER",
                "Error al guardar la orden en la base de datos",
                ex
            );
        } catch (Exception ex) {
            throw new DatabaseException(
                "Error inesperado al crear la orden",
                ex
            );
        }
    }
}


// ========================================
// EJEMPLO 8: Validaciones Personalizadas
// ========================================

@Service
public class RestaurantService {
    
    public RestaurantDTO createRestaurant(RestaurantDTO dto) {
        // ✅ Validar horarios
        if (dto.getOpeningTime() == null || dto.getClosingTime() == null) {
            throw new BadRequestException(
                "Se requieren horarios de apertura y cierre"
            );
        }
        
        if (dto.getOpeningTime().isAfter(dto.getClosingTime())) {
            throw new BadRequestException(
                "openingTime",
                dto.getOpeningTime(),
                "La hora de apertura debe ser antes que la de cierre"
            );
        }
        
        // ✅ Validar rating
        if (dto.getRating() != null && (dto.getRating() < 0 || dto.getRating() > 5)) {
            throw new BadRequestException(
                "rating",
                dto.getRating(),
                "El rating debe estar entre 0 y 5"
            );
        }
        
        // ... crear restaurante
    }
}


// ========================================
// RESUMEN DE MEJORES PRÁCTICAS
// ========================================

✅ SIEMPRE usar excepciones específicas (no RuntimeException genérico)
✅ INCLUIR detalles del recurso (nombre, campo, valor)
✅ MENSAJES descriptivos y claros
✅ VALIDAR permisos antes de operaciones sensibles
✅ VALIDAR existencia de recursos relacionados
✅ USAR try-catch solo para errores de BD o externos
✅ DEJAR que GlobalExceptionHandler maneje las excepciones

❌ NO usar Exception genérico
❌ NO ignorar excepciones (printStackTrace sin propagar)
❌ NO mensajes vagos ("Error", "Invalid")
❌ NO lanzar excepciones en constructores de DTOs
❌ NO capturar excepciones solo para loggear

*/
