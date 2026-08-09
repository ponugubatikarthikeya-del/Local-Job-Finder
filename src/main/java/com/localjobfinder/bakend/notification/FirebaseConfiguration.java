package com.localjobfinder.bakend.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class FirebaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

//    private final ObjectMapper objectMapper = new ObjectMapper();

    // Convert numbers and primitives to Strings automatically for FCM
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();


    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void init() throws IOException {

//        FileInputStream serviceAccount =
//                new FileInputStream("src/main/resources/firebase-service-account.json");

        InputStream serviceAccount;

        if(firebaseConfigPath.startsWith("classpath:")){
            String resourcePath = firebaseConfigPath.replace("classpath:","");
            serviceAccount = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if(serviceAccount == null){
                throw new IOException("Firebase Service Account file is not found in classpath: " + resourcePath);
            }

        } else {
            serviceAccount = new FileInputStream(firebaseConfigPath);
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }


    public void sendPushNotification(String fcmToken, String title, String body, String type, Object payload) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Map<String, String> dataMap = new HashMap<>();

            // Inject administrative click routing flags
            dataMap.put("notification_type", type);

            // 2. Map and parse dynamic object properties safely
            if (payload != null) {
                Map<String, Object> rawMap = objectMapper.convertValue(payload, new TypeReference<>() {});

                for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                    Object value = entry.getValue();
                    if (value == null) continue;

                    // 💡 CRUCIAL FIX: Catch floats or doubles (like duration metrics), round them, and pass as Integer string
                    if (value instanceof Double || value instanceof Float) {
                        double doubleValue = ((Number) value).doubleValue();
                        long roundedValue = Math.round(doubleValue);
                        dataMap.put(entry.getKey(), String.valueOf(roundedValue));
                    } else {
                        // Standard numbers and strings map cleanly across the wire
                        dataMap.put(entry.getKey(), String.valueOf(value));
                    }
                }
            }

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putAllData(dataMap) // Inject data map into message container
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM Notification sent successfully. Type: {}, ID: {}", type, response);
        } catch (Exception e) {
            log.error("FCM payload packaging distribution failed for token: {}", fcmToken, e);
        }
    }
}

