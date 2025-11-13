package upeu.edu.pe.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import upeu.edu.pe.restaurant.entity.Restaurant;
import upeu.edu.pe.restaurant.repository.RestaurantRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
    
    private final RestaurantRepository restaurantRepository;
    
    /**
     * Buscar restaurantes cercanos a una ubicación
     */
    public List<Map<String, Object>> findNearbyRestaurants(
            Double latitude, 
            Double longitude, 
            Double radiusKm) {
        
        List<Restaurant> allRestaurants = restaurantRepository.findByIsActiveTrue();
        
        List<Map<String, Object>> nearbyRestaurants = allRestaurants.stream()
                .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                .map(restaurant -> {
                    double distance = calculateDistance(
                            latitude, 
                            longitude, 
                            restaurant.getLatitude(), 
                            restaurant.getLongitude()
                    );
                    
                    if (radiusKm == null || distance <= radiusKm) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", restaurant.getId());
                        result.put("name", restaurant.getName());
                        result.put("description", restaurant.getDescription());
                        result.put("address", restaurant.getAddress());
                        result.put("phone", restaurant.getPhone());
                        result.put("rating", restaurant.getRating());
                        result.put("imageUrl", restaurant.getImageUrl());
                        result.put("coverImageUrl", restaurant.getCoverImageUrl());
                        result.put("latitude", restaurant.getLatitude());
                        result.put("longitude", restaurant.getLongitude());
                        result.put("averagePrice", restaurant.getAveragePrice());
                        result.put("openingHours", restaurant.getOpeningHours());
                        result.put("deliveryFee", restaurant.getDeliveryFee());
                        result.put("deliveryTime", restaurant.getDeliveryTime());
                        result.put("categories", restaurant.getCategories());
                        result.put("distance", Math.round(distance * 100.0) / 100.0);
                        result.put("distanceText", formatDistance(distance));
                        
                        return result;
                    }
                    
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(m -> (Double) m.get("distance")))
                .collect(Collectors.toList());
        
        return nearbyRestaurants;
    }
    
    /**
     * Obtener direcciones a un restaurante
     */
    public Map<String, Object> getDirections(
            Long restaurantId,
            Double fromLat,
            Double fromLng) {
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurante no encontrado"));
        
        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            throw new RuntimeException("El restaurante no tiene coordenadas GPS configuradas");
        }
        
        double distance = calculateDistance(
                fromLat, 
                fromLng, 
                restaurant.getLatitude(), 
                restaurant.getLongitude()
        );
        
        Map<String, Object> result = new HashMap<>();
        
        Map<String, Object> restaurantInfo = new HashMap<>();
        restaurantInfo.put("id", restaurant.getId());
        restaurantInfo.put("name", restaurant.getName());
        restaurantInfo.put("address", restaurant.getAddress());
        restaurantInfo.put("latitude", restaurant.getLatitude());
        restaurantInfo.put("longitude", restaurant.getLongitude());
        
        result.put("restaurant", restaurantInfo);
        result.put("distance", Math.round(distance * 100.0) / 100.0);
        result.put("distanceText", formatDistance(distance));
        result.put("estimatedTime", estimateTime(distance));
        result.put("googleMapsUrl", buildGoogleMapsUrl(
                restaurant.getLatitude(), 
                restaurant.getLongitude()
        ));
        
        return result;
    }
    
    /**
     * Calcular distancia entre dos puntos usando la fórmula de Haversine
     * @return distancia en kilómetros
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distance = EARTH_RADIUS_KM * c;
        
        return distance;
    }
    
    /**
     * Formatear distancia para mostrar
     */
    private String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            int meters = (int) (distanceKm * 1000);
            return meters + " m";
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }
    
    /**
     * Estimar tiempo de viaje basado en distancia
     */
    private String estimateTime(double distanceKm) {
        // Asumiendo velocidad promedio de 30 km/h en ciudad
        double hours = distanceKm / 30.0;
        int minutes = (int) Math.ceil(hours * 60);
        
        if (minutes < 5) {
            return "5 min";
        } else if (minutes < 60) {
            return minutes + " min";
        } else {
            int hrs = minutes / 60;
            int mins = minutes % 60;
            return hrs + " h " + mins + " min";
        }
    }
    
    /**
     * Construir URL de Google Maps para navegación
     */
    private String buildGoogleMapsUrl(Double lat, Double lng) {
        return String.format(
                "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                lat, lng
        );
    }
    
    /**
     * Buscar restaurantes por ubicación y filtros
     */
    public List<Map<String, Object>> searchRestaurants(
            Double latitude,
            Double longitude,
            Double radiusKm,
            Double minRating,
            String category) {
        
        List<Map<String, Object>> results = findNearbyRestaurants(latitude, longitude, radiusKm);
        
        // Filtrar por rating si se especifica
        if (minRating != null) {
            results = results.stream()
                    .filter(r -> {
                        Object ratingObj = r.get("rating");
                        if (ratingObj instanceof Number) {
                            double rating = ((Number) ratingObj).doubleValue();
                            return rating >= minRating;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        // Filtrar por categoría si se especifica
        if (category != null && !category.isEmpty()) {
            results = results.stream()
                    .filter(r -> {
                        @SuppressWarnings("unchecked")
                        Set<String> categories = (Set<String>) r.get("categories");
                        return categories != null && categories.stream()
                                .anyMatch(c -> c.toLowerCase().contains(category.toLowerCase()));
                    })
                    .collect(Collectors.toList());
        }
        
        return results;
    }
}
