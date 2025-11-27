package upeu.edu.pe.restaurant.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service.account.path:}")
    private String serviceAccountPath;

    @Value("${firebase.database.url:}")
    private String databaseUrl;

    @Bean
    public FirebaseApp initializeFirebase() {
        if (serviceAccountPath == null || serviceAccountPath.isEmpty()) {
            log.warn("Firebase service account path not configured. Push notifications will be disabled.");
            log.warn("Set FIREBASE_SERVICE_ACCOUNT_PATH environment variable to enable FCM.");
            return null;
        }

        try {
            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);

            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount));

            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                optionsBuilder.setDatabaseUrl(databaseUrl);
            }

            FirebaseOptions options = optionsBuilder.build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK initialized successfully");
                return app;
            } else {
                log.info("Firebase app already initialized");
                return FirebaseApp.getInstance();
            }

        } catch (IOException e) {
            log.error("❌ Error initializing Firebase Admin SDK: {}", e.getMessage());
            log.error("Push notifications will be disabled. Please check your configuration.");
            return null;
        }
    }
}
