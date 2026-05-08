package com.lifeplus.healthcare.modules.core;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepo;
    private boolean initialized = false;

    public NotificationService(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @PostConstruct
    public void init() {
        try {
            // Attempt to load from default location if it exists
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/firebase-service-account.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            initialized = true;
        } catch (IOException e) {
            System.err.println("Firebase Service Account not found. Push notifications will be logged only.");
        }
    }

    public void sendToTopic(String topic, String title, String body) {
        if (topic.startsWith("user_")) {
            try {
                Long userId = Long.parseLong(topic.substring(5));
                saveNotification(userId, title, body, "SYSTEM");
            } catch (Exception ignored) {}
        }
        
        if (!initialized) {
            System.out.println("[MOCK-FCM] Topic: " + topic + " | Title: " + title + " | Body: " + body);
            return;
        }
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveNotification(Long userId, String title, String message, String type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRead(false);
        notificationRepo.save(n);
    }

    public void sendToToken(String token, String title, String body) {
        if (!initialized) {
            System.out.println("[MOCK-FCM] Token: " + token + " | Title: " + title + " | Body: " + body);
            return;
        }
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
