package upeu.edu.pe.restaurant.controller;

import upeu.edu.pe.restaurant.entity.Coupon;
import upeu.edu.pe.restaurant.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Solo usuarios con rol ADMIN
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * GET /api/admin/stats
     * Obtener estadísticas generales del dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            Map<String, Object> stats = adminService.getGeneralStats();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/admin/recent-orders
     * Obtener las últimas órdenes del sistema
     */
    @GetMapping("/recent-orders")
    public ResponseEntity<?> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> orders = adminService.getRecentOrders(limit);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", orders
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * GET /api/admin/active-coupons
     * Obtener cupones activos
     */
    @GetMapping("/active-coupons")
    public ResponseEntity<?> getActiveCoupons() {
        try {
            List<Coupon> coupons = adminService.getActiveCoupons();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", coupons
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
