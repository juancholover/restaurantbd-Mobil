package upeu.edu.pe.restaurant.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import upeu.edu.pe.restaurant.entity.Restaurant;
import upeu.edu.pe.restaurant.entity.RestaurantSchedule;
import upeu.edu.pe.restaurant.repository.RestaurantScheduleRepository;
import upeu.edu.pe.restaurant.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantEnrichmentService {
    
    private final RestaurantScheduleRepository scheduleRepository;
    private final ReviewRepository reviewRepository;
    
    /**
     * Enriquece un restaurante con datos calculados dinámicamente
     */
    public void enrichRestaurant(Restaurant restaurant) {
        enrichWithScheduleInfo(restaurant);
        enrichWithReviewCount(restaurant);
    }
    
    /**
     * Enriquece una lista de restaurantes
     */
    public void enrichRestaurants(List<Restaurant> restaurants) {
        restaurants.forEach(this::enrichRestaurant);
    }
    
    /**
     * Calcula si el restaurante está abierto ahora y el horario de hoy
     */
    private void enrichWithScheduleInfo(Restaurant restaurant) {
        LocalDateTime now = LocalDateTime.now();
        RestaurantSchedule.DayOfWeek today = getCurrentDayOfWeek(now);
        
        Optional<RestaurantSchedule> scheduleOpt = scheduleRepository
            .findByRestaurantIdAndDayOfWeek(restaurant.getId(), today);
        
        if (scheduleOpt.isPresent()) {
            RestaurantSchedule schedule = scheduleOpt.get();
            
            if (schedule.getIsClosed()) {
                restaurant.setIsOpenNow(false);
                restaurant.setTodaySchedule("Cerrado");
            } else {
                LocalTime currentTime = now.toLocalTime();
                boolean isOpen = isWithinSchedule(currentTime, schedule.getOpenTime(), schedule.getCloseTime());
                
                restaurant.setIsOpenNow(isOpen);
                restaurant.setTodaySchedule(formatSchedule(schedule.getOpenTime(), schedule.getCloseTime()));
            }
        } else {
            // Si no hay horario definido, usar el horario por defecto
            restaurant.setIsOpenNow(true);
            restaurant.setTodaySchedule(restaurant.getOpeningHours());
        }
    }
    
    /**
     * Agrega el contador de reseñas
     */
    private void enrichWithReviewCount(Restaurant restaurant) {
        long count = reviewRepository.countByRestaurantId(restaurant.getId());
        restaurant.setReviewCount((int) count);
    }
    
    /**
     * Convierte LocalDateTime a DayOfWeek del enum
     */
    private RestaurantSchedule.DayOfWeek getCurrentDayOfWeek(LocalDateTime dateTime) {
        java.time.DayOfWeek javaDayOfWeek = dateTime.getDayOfWeek();
        return RestaurantSchedule.DayOfWeek.valueOf(javaDayOfWeek.name());
    }
    
    /**
     * Verifica si la hora actual está dentro del horario de apertura
     */
    private boolean isWithinSchedule(LocalTime current, LocalTime open, LocalTime close) {
        // Si cierra después de medianoche
        if (close.isBefore(open)) {
            return current.isAfter(open) || current.isBefore(close);
        }
        // Horario normal
        return current.isAfter(open) && current.isBefore(close);
    }
    
    /**
     * Formatea el horario para mostrar
     */
    private String formatSchedule(LocalTime open, LocalTime close) {
        return String.format("%s - %s", 
            formatTime(open), 
            formatTime(close));
    }
    
    /**
     * Formatea una hora en formato 12 horas
     */
    private String formatTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        String amPm = hour >= 12 ? "PM" : "AM";
        
        if (hour > 12) {
            hour -= 12;
        } else if (hour == 0) {
            hour = 12;
        }
        
        return String.format("%d:%02d %s", hour, minute, amPm);
    }
}
