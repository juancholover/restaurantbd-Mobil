package upeu.edu.pe.restaurant.service;

import upeu.edu.pe.restaurant.entity.Order;
import upeu.edu.pe.restaurant.entity.Coupon;
import upeu.edu.pe.restaurant.repository.OrderRepository;
import upeu.edu.pe.restaurant.repository.UserRepository;
import upeu.edu.pe.restaurant.repository.MenuItemRepository;
import upeu.edu.pe.restaurant.repository.CouponRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private CouponRepository couponRepository;

   
    public Map<String, Object> getGeneralStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total de órdenes
        long totalOrders = orderRepository.count();
        
        // Ingresos totales (suma de todas las órdenes con estado DELIVERED)
        Double totalRevenue = orderRepository.sumTotalAmountByStatus(Order.Status.DELIVERED);
        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }
        
        // Usuarios activos (con al menos una orden)
        long activeUsers = orderRepository.countDistinctUsers();
        
        // Total de productos en el menú
        long totalProducts = menuItemRepository.count();
        
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("activeUsers", activeUsers);
        stats.put("totalProducts", totalProducts);
        
        return stats;
    }
    
    
    public List<Map<String, Object>> getRecentOrders(int limit) {
        List<Order> orders = orderRepository.findRecentOrders(PageRequest.of(0, limit));
        
        return orders.stream().map(order -> {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", order.getId());
            orderData.put("orderNumber", "#ORD-" + order.getId());
            orderData.put("userName", order.getUser().getName());
            orderData.put("userEmail", order.getUser().getEmail());
            orderData.put("status", order.getStatus().toString());
            orderData.put("totalAmount", order.getTotalAmount());
            orderData.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "cash");
            orderData.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus() : "pending");
            orderData.put("createdAt", order.getCreatedAt());
            orderData.put("restaurantName", order.getRestaurant().getName());
            
            return orderData;
        }).collect(Collectors.toList());
    }
    
   
    public List<Coupon> getActiveCoupons() {
        return couponRepository.findActiveCoupons(LocalDateTime.now());
    }
}
