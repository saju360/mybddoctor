package com.lifeplus.healthcare.modules.core;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController {
    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;

    ReviewController(ReviewRepository reviewRepo, UserRepository userRepo) {
        this.reviewRepo = reviewRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    List<Review> getReviews(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long id
    ) {
        if (type != null && id != null) {
            return reviewRepo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(type, id);
        }
        if (type != null && !type.isBlank()) {
            return reviewRepo.findAll().stream()
                    .filter(r -> type.equalsIgnoreCase(r.getEntityType()))
                    .toList();
        }
        return reviewRepo.findAll();
    }

    @PostMapping
    Review submitReview(
            @RequestBody Review body,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser
    ) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        if (body.getEntityType() == null || body.getEntityType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "entityType is required");
        }
        if (body.getEntityId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "entityId is required");
        }
        if (body.getRating() < 1 || body.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rating must be between 1 and 5");
        }

        body.setId(null);
        body.setUserId(userId);
        if (body.getUserName() == null || body.getUserName().isBlank()) {
            String fallback = userRepo.findById(userId).map(User::getFullName).orElse("User");
            body.setUserName(fallback);
        }
        return reviewRepo.save(body);
    }

    @DeleteMapping("/{id}")
    void deleteReview(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser
    ) {
        ControllerGuards.ensureAdmin("Review delete");
        Review existing = reviewRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        reviewRepo.delete(existing);
    }
}

@RestController
@RequestMapping("/api/v1/chat")
class ChatController {
    private final ChatRoomRepository roomRepo;
    private final ChatMessageRepository messageRepo;

    ChatController(ChatRoomRepository roomRepo, ChatMessageRepository messageRepo) {
        this.roomRepo = roomRepo;
        this.messageRepo = messageRepo;
    }

    @GetMapping("/rooms")
    List<ChatRoom> getRooms(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        return roomRepo.findMyRooms(userId);
    }

    @GetMapping("/rooms/all")
    List<ChatRoom> getAllRooms(@RequestHeader(value = "X-Auth-User", required = false) String authUser) {
        ControllerGuards.ensureAdmin("Chat rooms list");
        return roomRepo.findAll().stream()
                .sorted((a, b) -> {
                    LocalDateTime at = a.getLastMessageTime() == null ? LocalDateTime.MIN : a.getLastMessageTime();
                    LocalDateTime bt = b.getLastMessageTime() == null ? LocalDateTime.MIN : b.getLastMessageTime();
                    return bt.compareTo(at);
                })
                .toList();
    }

    @GetMapping("/messages/{roomId}")
    List<ChatMessage> getMessages(
            @PathVariable Long roomId,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser
    ) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        ChatRoom room = roomRepo.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found"));
        ensureParticipant(room, userId, "Read messages");
        return messageRepo.findByRoomIdOrderByTimestampAsc(roomId);
    }

    @PostMapping("/send")
    ChatMessage sendMessage(
            @RequestBody ChatMessage body,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser
    ) {
        Long userId = ControllerGuards.ensureSignedInAndGetId(authUser);
        if (body.getRoomId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roomId is required");
        }
        if (body.getMessage() == null || body.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message is required");
        }

        ChatRoom room = roomRepo.findById(body.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found"));
        ensureParticipant(room, userId, "Send message");

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(room.getId());
        msg.setSenderId(userId);
        msg.setMessage(body.getMessage().trim());
        msg.setTimestamp(LocalDateTime.now());
        msg.setRead(false);

        ChatMessage saved = messageRepo.save(msg);
        room.setLastMessage(saved.getMessage());
        room.setLastMessageTime(saved.getTimestamp());
        roomRepo.save(room);
        return saved;
    }

    @PostMapping("/start/{userId}")
    ChatRoom startChat(
            @PathVariable Long userId,
            @RequestHeader(value = "X-Auth-User", required = false) String authUser
    ) {
        Long actorId = ControllerGuards.ensureSignedInAndGetId(authUser);
        if (actorId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start chat with self");
        }
        return roomRepo.findBetween(actorId, userId).orElseGet(() -> {
            ChatRoom room = new ChatRoom();
            room.setParticipantOneId(actorId);
            room.setParticipantTwoId(userId);
            room.setLastMessage("");
            room.setLastMessageTime(LocalDateTime.now());
            return roomRepo.save(room);
        });
    }

    private void ensureParticipant(ChatRoom room, Long userId, String action) {
        boolean allowed = userId.equals(room.getParticipantOneId()) || userId.equals(room.getParticipantTwoId());
        if (!allowed && !ControllerGuards.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, action + " is allowed only for participants or admin");
        }
    }
}
