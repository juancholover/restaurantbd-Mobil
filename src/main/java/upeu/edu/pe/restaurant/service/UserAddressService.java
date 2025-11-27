package upeu.edu.pe.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.restaurant.dto.UserAddressDTO;
import upeu.edu.pe.restaurant.entity.User;
import upeu.edu.pe.restaurant.entity.UserAddress;
import upeu.edu.pe.restaurant.repository.UserAddressRepository;
import upeu.edu.pe.restaurant.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressService {
    
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;
    
    /**
     * Obtener todas las direcciones de un usuario
     */
    public List<UserAddressDTO> getUserAddresses(Long userId) {
        List<UserAddress> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crear nueva dirección
     */
    @Transactional
    public UserAddressDTO createAddress(Long userId, UserAddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Si es dirección por defecto, desactivar las demás
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            addressRepository.unsetAllDefaultAddresses(userId);
        }
        
        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setAddress(addressDTO.getAddress());
        address.setPhone(addressDTO.getPhone());
        address.setLabel(addressDTO.getLabel());
        address.setIsDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : false);
        address.setLatitude(addressDTO.getLatitude());
        address.setLongitude(addressDTO.getLongitude());
        
        UserAddress savedAddress = addressRepository.save(address);
        
        log.info("✅ Dirección creada para usuario ID: {}", userId);
        
        return convertToDTO(savedAddress);
    }
    
    /**
     * Actualizar dirección existente
     */
    @Transactional
    public UserAddressDTO updateAddress(Long userId, Long addressId, UserAddressDTO addressDTO) {
        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));
        
        // Si se está marcando como default, desactivar las demás
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() && !address.getIsDefault()) {
            addressRepository.unsetAllDefaultAddresses(userId);
        }
        
        address.setAddress(addressDTO.getAddress());
        address.setPhone(addressDTO.getPhone());
        address.setLabel(addressDTO.getLabel());
        address.setIsDefault(addressDTO.getIsDefault() != null ? addressDTO.getIsDefault() : address.getIsDefault());
        address.setLatitude(addressDTO.getLatitude());
        address.setLongitude(addressDTO.getLongitude());
        
        UserAddress updatedAddress = addressRepository.save(address);
        
        log.info("✅ Dirección actualizada ID: {} para usuario ID: {}", addressId, userId);
        
        return convertToDTO(updatedAddress);
    }
    
    /**
     * Eliminar dirección
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        if (!addressRepository.existsByIdAndUserId(addressId, userId)) {
            throw new RuntimeException("Dirección no encontrada");
        }
        
        addressRepository.deleteById(addressId);
        
        log.info("🗑️ Dirección eliminada ID: {} de usuario ID: {}", addressId, userId);
    }
    
    /**
     * Convertir entidad a DTO
     */
    private UserAddressDTO convertToDTO(UserAddress address) {
        UserAddressDTO dto = new UserAddressDTO();
        dto.setId(address.getId());
        dto.setAddress(address.getAddress());
        dto.setPhone(address.getPhone());
        dto.setLabel(address.getLabel());
        dto.setIsDefault(address.getIsDefault());
        dto.setLatitude(address.getLatitude());
        dto.setLongitude(address.getLongitude());
        return dto;
    }
}
