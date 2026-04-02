package com.codegym.be_kafka.controller;

import com.codegym.be_kafka.service.ChatProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ============================================================
 * CHAT CONTROLLER
 * ============================================================
 * REST API phục vụ demo Kafka.
 *
 * Endpoints:
 *   POST /api/send          → Gửi 1 message từ 1 user
 *   POST /api/spam          → Giả lập 10 user × 100 message song song
 *   GET  /api/topic-info    → Trả về thông tin cấu hình topic
 * ============================================================
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChatController {

    private final ChatProducerService producerService;

    // ─────────────────────────────────────────────────────────
    // POST /api/send
    // Body: { "userId": "user-1", "content": "Hello World" }
    //
    // Flow: Frontend → Controller → ProducerService → Kafka
    //       Kafka → ConsumerService → WebSocket → Frontend
    // ─────────────────────────────────────────────────────────
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(@RequestBody Map<String, String> body) {
        try {
            String userId  = body.getOrDefault("userId", "anonymous");
            String content = body.getOrDefault("content", "");

            if (content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Content is empty"));
            }

            log.info("[API] /send request from user={}: {}", userId, content);
            producerService.send(userId, content);
            
            return ResponseEntity.ok(Map.of(
                    "status",  "sent",
                    "userId",  userId,
                    "content", content
            ));
        } catch (Exception e) {
            log.error("[API] Error in /send endpoint: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Internal Server Error",
                    "message", e.getMessage()
            ));
        }
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/spam
    // Body: { "users": 10, "messagesPerUser": 100 } (optional, defaults above)
    //
    // DEMO CONCURRENCY:
    //   - Tạo thread pool với N_USERS threads
    //   - Mỗi thread đại diện 1 user, gửi M_MSG messages liên tục
    //   - Tất cả threads chạy CÙNG LÚC
    //
    // Kafka behavior bạn sẽ thấy trong log:
    //   1. Producer logs xuất hiện không theo thứ tự user
    //      → Vì threads tranh nhau CPU
    //   2. Consumer logs: cùng userId → luôn CÙNG partition
    //      → Vì key routing: hash(userId) % partitions
    //   3. Offset tăng dần trong từng partition
    //      → Thứ tự TRONG partition được đảm bảo
    // ─────────────────────────────────────────────────────────
    @PostMapping("/spam")
    public ResponseEntity<Map<String, Object>> spam(@RequestBody(required = false) Map<String, Integer> body) {
        int nUsers  = (body != null && body.containsKey("users"))           ? body.get("users")           : 10;
        int nMsgs   = (body != null && body.containsKey("messagesPerUser")) ? body.get("messagesPerUser") : 100;

        log.info("[Spam] Starting {} users × {} messages = {} total messages",
                nUsers, nMsgs, (long) nUsers * nMsgs);

        ExecutorService pool = Executors.newFixedThreadPool(nUsers);

        for (int u = 1; u <= nUsers; u++) {
            final String userId = "user-" + u;
            pool.submit(() -> {
                for (int m = 1; m <= nMsgs; m++) {
                    producerService.send(userId, "msg-" + m);
                }
                log.info("[Spam] {} finished sending {} messages", userId, nMsgs);
            });
        }

        pool.shutdown();

        // Không chờ hoàn thành, trả về ngay để frontend không bị block
        return ResponseEntity.ok(Map.of(
                "status",          "spam started",
                "users",           nUsers,
                "messagesPerUser", nMsgs,
                "totalMessages",   (long) nUsers * nMsgs
        ));
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/topic-info
    // Trả về thông tin cấu hình hiện tại để frontend hiển thị
    // ─────────────────────────────────────────────────────────
    @GetMapping("/topic-info")
    public ResponseEntity<Map<String, Object>> topicInfo() {
        return ResponseEntity.ok(Map.of(
                "topic",      "chat-topic",
                "partitions", 3,
                "groupId",    "chat-group",
                "concurrency", 3
        ));
    }
}
