package com.codegym.be_kafka.service;

import com.codegym.be_kafka.model.ChatMessage;
import com.codegym.be_kafka.model.ConsumedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * ============================================================
 * KAFKA CONSUMER SERVICE
 * ============================================================
 * Kafka Consumer chạy với concurrency=3 (1 thread / partition).
 *
 * QUAN TRỌNG - HIỂU ĐÚNG VỀ CONCURRENCY:
 * ─────────────────────────────────────────────────────────────
 * 1. concurrency=3 → Spring tạo 3 KafkaMessageListenerContainer
 *    → Mỗi container chạy 1 thread riêng (consumer-0-C-1, 2, 3)
 *    → Mỗi thread phụ trách 1 partition
 *
 * 2. KHÔNG có race condition với Kafka:
 *    → Mỗi partition chỉ có 1 thread đọc
 *    → Thứ tự trong partition được ĐẢM BẢO
 *
 * 3. 10 user gửi cùng lúc:
 *    → Producer ghi vào các partition song song (không thứ tự)
 *    → Nhưng trong TỪNG partition: message đến trước, xử lý trước
 *    → Giữa các partition: KHÔNG CÓ THỨ TỰ CHUNG
 *
 * LOG FORMAT: [Consumer-X] partition=Y offset=Z msg=W
 * ─────────────────────────────────────────────────────────────
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @KafkaListener(
            topics   = "chat-topic",
            groupId  = "chat-group",
            concurrency = "10"
    )
    public void consume(org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        try {
            String jsonContent = record.value();
            ChatMessage msg    = objectMapper.readValue(jsonContent, ChatMessage.class);
            
            int         partition = record.partition();
            long        offset    = record.offset();
            String      thread    = Thread.currentThread().getName();

            log.info("[Consumer] thread={} partition={} offset={} user={} msg=\"{}\"",
                    thread, partition, offset, msg.getUserId(), msg.getContent());

            ConsumedMessage consumed = new ConsumedMessage(
                    msg.getUserId(),
                    msg.getContent(),
                    partition,
                    offset,
                    thread,
                    msg.getTimestamp()
            );

            messagingTemplate.convertAndSend("/topic/messages", consumed);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("[Consumer] JSON Deserialization failed: {}", e.getMessage());
        }
    }
}
