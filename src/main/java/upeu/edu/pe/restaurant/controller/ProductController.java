package upeu.edu.pe.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upeu.edu.pe.restaurant.dto.ProductDTO;
import upeu.edu.pe.restaurant.dto.response.ApiResponse;
import upeu.edu.pe.restaurant.entity.MenuItem;
import upeu.edu.pe.restaurant.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Productos obtenidos exitosamente", products)
        );
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Producto obtenido exitosamente", product)
        );
    }
    
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getProductsByRestaurant(
            @PathVariable Long restaurantId) {
        List<ProductDTO> products = productService.getProductsByRestaurant(restaurantId);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Productos del restaurante obtenidos exitosamente", products)
        );
    }
    
    @PostMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @PathVariable Long restaurantId,
            @RequestBody MenuItem menuItem) {
        ProductDTO created = productService.createProduct(restaurantId, menuItem);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Producto creado exitosamente", created)
        );
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestBody MenuItem menuItem) {
        ProductDTO updated = productService.updateProduct(id, menuItem);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Producto actualizado exitosamente", updated)
        );
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Producto eliminado exitosamente", null)
        );
    }
}
