package upeu.edu.pe.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import upeu.edu.pe.restaurant.entity.RestaurantSchedule;
import upeu.edu.pe.restaurant.entity.RestaurantSchedule.DayOfWeek;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantScheduleRepository extends JpaRepository<RestaurantSchedule, Long> {
    
    List<RestaurantSchedule> findByRestaurantId(Long restaurantId);
    
    Optional<RestaurantSchedule> findByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);
    
    @Query("SELECT rs FROM RestaurantSchedule rs WHERE rs.restaurant.id = :restaurantId AND rs.dayOfWeek = :dayOfWeek AND rs.isClosed = false")
    Optional<RestaurantSchedule> findActiveSchedule(Long restaurantId, DayOfWeek dayOfWeek);
    
    void deleteByRestaurantId(Long restaurantId);
}
