package upeu.edu.pe.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.CreateOrderRequest;
import upeu.edu.pe.restaurant.dto.OrderDTO;
import upeu.edu.pe.restaurant.dto.OrderItemDTO;
import upeu.edu.pe.restaurant.entity.*;
import upeu.edu.pe.restaurant.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;
    
    @Transactional
    public OrderDTO createOrder(String userEmail, CreateOrderRequest request) {
        // Obtener usuario
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Obtener restaurante
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));
        
        // Crear orden
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setTotalAmount(request.getTotalAmount());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setNotes(request.getNotes());
        
        // ✅ Si tiene Payment Intent ID, marcar como CONFIRMED (pago exitoso)
        if (request.getPaymentIntentId() != null && !request.getPaymentIntentId().isEmpty()) {
            order.setStatus(Order.Status.CONFIRMED);
            order.setPaymentStatus("completed");
            order.setPaymentIntentId(request.getPaymentIntentId());
            order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "card");
        } else {
            // Sin pago con tarjeta, queda como PENDING
            order.setStatus(Order.Status.PENDING);
            order.setPaymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "pending");
            order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "cash");
        }
        
        // Guardar orden primero
        Order savedOrder = orderRepository.save(order);
        
        // Crear items de la orden - Evitar ConcurrentModificationException
        // Crear lista temporal primero
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(itemRequest.getPrice());
            
            // Guardar item individualmente
            orderItemRepository.save(orderItem);
            
            // Agregar usando el método auxiliar para evitar ConcurrentModificationException
            savedOrder.addOrderItem(orderItem);
        }
        
        // Guardar orden con items
        Order finalOrder = orderRepository.save(savedOrder);
        
        return convertToDTO(finalOrder);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para ver esta orden");
        }
        
        return convertToDTO(order);
    }
    
    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        
        try {
            order.setStatus(Order.Status.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de orden inválido");
        }
        
        Order updated = orderRepository.save(order);
        
        // 🔔 Enviar notificación al usuario
        notificationService.notifyOrderStatusChange(
                updated.getUser().getId(),
                updated.getId(),
                status
        );
        
        return convertToDTO(updated);
    }
    
    @Transactional
    public OrderDTO updateOrderStatus(Long id, String status, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        
        // Validar estados permitidos
        try {
            Order.Status newStatus = Order.Status.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + status);
        }
        
        Order updated = orderRepository.save(order);
        
        // 🔔 Enviar notificación al usuario
        notificationService.notifyOrderStatusChange(
                updated.getUser().getId(),
                updated.getId(),
                status
        );
        
        return convertToDTO(updated);
    }
    
    @Transactional
    public OrderDTO cancelOrder(Long id, String reason, String userEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        
        // Solo se puede cancelar si no está entregada
        if (order.getStatus() == Order.Status.DELIVERED) {
            throw new RuntimeException("No se puede cancelar una orden ya entregada");
        }
        
        order.setStatus(Order.Status.CANCELLED);
        order.setCancellationReason(reason);
        
        Order updated = orderRepository.save(order);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void updatePaymentStatus(String paymentIntentId, String newStatus) {
        // Buscar orden por Payment Intent ID
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> paymentIntentId.equals(o.getPaymentIntentId()))
                .toList();
        
        if (!orders.isEmpty()) {
            Order order = orders.get(0);
            order.setPaymentStatus(newStatus);
            
            // Si el pago fue exitoso, cambiar a CONFIRMED
            if ("completed".equalsIgnoreCase(newStatus)) {
                order.setStatus(Order.Status.CONFIRMED);
            }
            
            orderRepository.save(order);
        }
    }
    
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getName());
        dto.setRestaurantId(order.getRestaurant().getId());
        dto.setRestaurantName(order.getRestaurant().getName());
        dto.setRestaurantImage(order.getRestaurant().getImageUrl());
        dto.setStatus(order.getStatus().name());
        dto.setSubtotal(order.getTotalAmount()); // Simplificado
        dto.setDeliveryFee(java.math.BigDecimal.valueOf(2.50));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setNotes(order.getNotes());
        dto.setPaymentIntentId(order.getPaymentIntentId());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setCancellationReason(order.getCancellationReason());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Convertir items
        List<OrderItemDTO> items = order.getOrderItems()
                .stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(items);
        
        return dto;
    }
    
    private OrderItemDTO convertItemToDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getMenuItem().getId());
        dto.setProductName(orderItem.getMenuItem().getName());
        dto.setProductImage(orderItem.getMenuItem().getImageUrl());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        return dto;
    }
}
