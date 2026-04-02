package com.codegym.be_kafka.service;

import com.codegym.be_kafka.config.KafkaTopicConfig;
import com.codegym.be_kafka.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * ============================================================
 * KAFKA PRODUCER SERVICE
 * ============================================================
 * Nhiệm vụ: Gửi ChatMessage vào Kafka topic.
 *
 * KEY = userId → Kafka dùng hash(key) % 3 để chọn partition.
 *   Ví dụ:
 *     user-1 → hash("user-1") % 3 → partition 2
 *     user-2 → hash("user-2") % 3 → partition 0
 *     user-1 gửi message lần 2 → vẫn vào partition 2
 *
 * → Cùng userId luôn vào CÙNG partition
 * → Trong 1 partition, message được lưu theo thứ tự ghi vào
 * → Với 10 user gửi cùng lúc:
 *     Không có "ai được xử lý trước" ở cấp Topic.
 *     Chỉ có thứ tự TRONG TỪNG partition.
 * ============================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public void send(String userId, String content) {
        try {
            ChatMessage message = new ChatMessage(userId, content, System.currentTimeMillis());
            String jsonMessage = objectMapper.writeValueAsString(message);

            log.info("[Producer] Sending JSON message for user {}: {}", userId, jsonMessage);
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(KafkaTopicConfig.TOPIC_NAME, userId, jsonMessage);

            future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[Producer] FAILED user={} msg={} error={}", userId, content, ex.getMessage());
                return;
            }
            int  partition = result.getRecordMetadata().partition();
            long offset    = result.getRecordMetadata().offset();
            // LOG FORMAT yêu cầu: [Producer] user=X sent msg=Y partition=Z
                log.info("[Producer] user={} sent msg=\"{}\" partition={} offset={}",
                        userId, content, partition, offset);
            });
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("[Producer] JSON Serialization failed for user={}: {}", userId, e.getMessage());
        }
    }
}
