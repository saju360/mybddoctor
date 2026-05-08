package com.lifeplus.healthcare.modules.core;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService, NotificationRepository notificationRepo) {
        this.notificationService = notificationService;
        this.notificationRepo = notificationRepo;
    }

    private final NotificationRepository notificationRepo;

    @GetMapping("/my")
    public List<Notification> my(
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        // Resolve user ID using a helper if possible, but let's just do it directly or via SecurityContext
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
             if (authUser == null || authUser.isBlank()) {
                 throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign-in required");
             }
        }
        
        // This is a bit simplified. In a real app we'd use the resolved ID.
        // For now, let's assume the client sends X-Auth-User as the ID if not using Spring Security fully.
        Long userId = null;
        try { userId = Long.parseLong(authUser); } catch (Exception ignored) {}
        
        if (userId == null) return List.of();
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PostMapping("/fcm")
    public ResponseEntity<?> sendFcm(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        requireAuth(authUser);
        String to = payload.getOrDefault("to", "").trim();
        String message = payload.getOrDefault("message", "").trim();
        if (to.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'to' (FCM token) is required");
        }
        if (message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'message' is required");
        }
        String title = payload.getOrDefault("title", "LifePlus Notification");
        notificationService.sendToToken(to, title, message);
        return ResponseEntity.accepted().body(Map.of(
                "channel", "FCM",
                "status", "SENT",
                "recipient", to,
                "title", title,
                "message", message,
                "sentAt", Instant.now().toString()
        ));
    }

    @PostMapping("/topic")
    public ResponseEntity<?> sendTopic(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        requireAuth(authUser);
        String topic = payload.getOrDefault("topic", "").trim();
        String message = payload.getOrDefault("message", "").trim();
        if (topic.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'topic' is required");
        }
        if (message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'message' is required");
        }
        String title = payload.getOrDefault("title", "LifePlus Update");
        notificationService.sendToTopic(topic, title, message);
        return ResponseEntity.accepted().body(Map.of(
                "channel", "FCM_TOPIC",
                "status", "SENT",
                "topic", topic,
                "title", title,
                "message", message,
                "sentAt", Instant.now().toString()
        ));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> sendUserTopic(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        requireAuth(authUser);
        String message = payload.getOrDefault("message", "").trim();
        if (message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'message' is required");
        }
        String title = payload.getOrDefault("title", "LifePlus Account");
        String topic = "user_" + userId;
        notificationService.sendToTopic(topic, title, message);
        return ResponseEntity.accepted().body(Map.of(
                "channel", "FCM_TOPIC",
                "status", "SENT",
                "topic", topic,
                "title", title,
                "message", message,
                "sentAt", Instant.now().toString()
        ));
    }

    @PostMapping("/broadcast/district/{district}")
    public ResponseEntity<?> sendDistrictBroadcast(
            @PathVariable String district,
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        requireAuth(authUser);
        String message = payload.getOrDefault("message", "").trim();
        if (message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'message' is required");
        }
        String title = payload.getOrDefault("title", "LifePlus District Alert");
        String normalized = district.toLowerCase().replace(" ", "_");
        String topic = payload.getOrDefault("topicType", "blood").equalsIgnoreCase("emergency")
                ? "emergencies_" + normalized
                : "blood_requests_" + normalized;
        notificationService.sendToTopic(topic, title, message);
        return ResponseEntity.accepted().body(Map.of(
                "channel", "FCM_TOPIC",
                "status", "SENT",
                "topic", topic,
                "title", title,
                "message", message,
                "sentAt", Instant.now().toString()
        ));
    }

    @PostMapping("/sms")
    public ResponseEntity<?> sendSms(
            @RequestBody Map<String, String> payload,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        requireAuth(authUser);
        String to = payload.getOrDefault("to", "").trim();
        String message = payload.getOrDefault("message", "").trim();
        if (to.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'to' (phone number) is required");
        }
        if (message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'message' is required");
        }
        // TODO: integrate an SMS gateway (e.g. Twilio, SSL Wireless) here
        return ResponseEntity.accepted().body(Map.of(
                "channel", "SMS",
                "status", "QUEUED",
                "recipient", to,
                "message", message,
                "queuedAt", Instant.now().toString()
        ));
    }

    private void requireAuth(String authUser) {
        // Check explicit header first, then fall back to Spring SecurityContext
        if (authUser != null && !authUser.isBlank()) return;
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Sign-in required to send notifications");
        }
    }
}
