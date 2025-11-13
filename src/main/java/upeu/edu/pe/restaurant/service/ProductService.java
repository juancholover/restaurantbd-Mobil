package upeu.edu.pe.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.ProductDTO;
import upeu.edu.pe.restaurant.entity.MenuItem;
import upeu.edu.pe.restaurant.entity.Restaurant;
import upeu.edu.pe.restaurant.repository.MenuItemRepository;
import upeu.edu.pe.restaurant.repository.RestaurantRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return convertToDTO(menuItem);
    }
    
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return menuItemRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private ProductDTO convertToDTO(MenuItem menuItem) {
        ProductDTO dto = new ProductDTO();
        dto.setId(menuItem.getId());
        dto.setRestaurantId(menuItem.getRestaurant().getId());
        dto.setRestaurantName(menuItem.getRestaurant().getName());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setCategory(menuItem.getCategory());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setRating(java.math.BigDecimal.valueOf(4.5)); // Rating por defecto
        dto.setIsAvailable(menuItem.getIsAvailable());
        dto.setOptions(List.of()); // Sin opciones por ahora
        return dto;
    }
    
    @Transactional
    public ProductDTO createProduct(Long restaurantId, MenuItem menuItem) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));
        
        menuItem.setRestaurant(restaurant);
        menuItem.setIsAvailable(true);
        MenuItem saved = menuItemRepository.save(menuItem);
        return convertToDTO(saved);
    }
    
    @Transactional
    public ProductDTO updateProduct(Long id, MenuItem menuItem) {
        MenuItem existing = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        existing.setName(menuItem.getName());
        existing.setDescription(menuItem.getDescription());
        existing.setPrice(menuItem.getPrice());
        existing.setCategory(menuItem.getCategory());
        existing.setImageUrl(menuItem.getImageUrl());
        existing.setIsAvailable(menuItem.getIsAvailable());
        
        MenuItem updated = menuItemRepository.save(existing);
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado");
        }
        menuItemRepository.deleteById(id);
    }
}
