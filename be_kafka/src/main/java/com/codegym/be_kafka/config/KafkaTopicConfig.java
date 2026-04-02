package com.codegym.be_kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * ============================================================
 * KAFKA TOPIC CONFIG
 * ============================================================
 * Tạo topic "chat-topic" với:
 *   - 3 partitions → cho phép 3 consumer chạy song song
 *   - replicationFactor = 1 → single broker (dev/demo)
 *
 * TẠI SAO 3 PARTITIONS?
 *   Với 3 partitions, Kafka chia nhỏ dữ liệu.
 *   key = userId → cùng userId luôn vào CÙNG partition
 *   → đảm bảo thứ tự message của 1 user KHÔNG bị đảo
 * ============================================================
 */
@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_NAME = "chat-topic";
    public static final int    PARTITIONS = 10;

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name(TOPIC_NAME)
                .partitions(PARTITIONS)
                .replicas(1)
                .build();
    }
}
