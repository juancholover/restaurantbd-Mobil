package upeu.edu.pe.restaurant.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.CreateOrderRequest;
import upeu.edu.pe.restaurant.dto.OrderDTO;
import upeu.edu.pe.restaurant.dto.response.ApiResponse;
import upeu.edu.pe.restaurant.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        OrderDTO order = orderService.createOrder(userEmail, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orden creada exitosamente", order)
        );
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<OrderDTO> orders = orderService.getUserOrders(userEmail);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Órdenes obtenidas exitosamente", orders)
        );
    }
    
    // Alias para getOrders (GET /api/orders/my)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(
            Authentication authentication) {
        return getUserOrders(authentication);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        OrderDTO order = orderService.getOrderById(id, userEmail);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orden obtenida exitosamente", order)
        );
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        String userEmail = authentication.getName();
        OrderDTO order = orderService.updateOrderStatus(id, status, userEmail);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Estado de orden actualizado exitosamente", order)
        );
    }
    
    @PostMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<OrderDTO>> markAsDelivered(
            @PathVariable Long id,
            Authentication authentication) {
        String userEmail = authentication.getName();
        OrderDTO order = orderService.updateOrderStatus(id, "DELIVERED", userEmail);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orden marcada como entregada", order)
        );
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        String userEmail = authentication.getName();
        OrderDTO order = orderService.cancelOrder(id, reason, userEmail);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orden cancelada exitosamente", order)
        );
    }
}
