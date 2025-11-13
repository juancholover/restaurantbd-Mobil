package upeu.edu.pe.restaurant.repository;

import upeu.edu.pe.restaurant.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByRestaurantId(Long restaurantId);
    List<Reservation> findByUserIdOrderByReservationDateDesc(Long userId);
}
