package upeu.edu.pe.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.restaurant.entity.UserAddress;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    
    List<UserAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT ua FROM UserAddress ua WHERE ua.user.id = :userId AND ua.isDefault = true")
    Optional<UserAddress> findDefaultAddressByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE UserAddress ua SET ua.isDefault = false WHERE ua.user.id = :userId")
    void unsetAllDefaultAddresses(@Param("userId") Long userId);
    
    boolean existsByIdAndUserId(Long id, Long userId);
}
