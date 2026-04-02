package com.codegym.be_kafka.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * ============================================================
 * WEBSOCKET CONFIG (STOMP)
 * ============================================================
 * Frontend kết nối WS để nhận message REAL-TIME từ Consumer.
 *
 * Flow:
 *   Consumer nhận message từ Kafka
 *   → gửi tới "/topic/messages" qua SimpMessagingTemplate
 *   → Frontend subscribe "/topic/messages" nhận ngay lập tức
 * ============================================================
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Broker nội bộ, frontend subscribe prefix /topic
        registry.enableSimpleBroker("/topic");
        // Prefix để frontend gửi message VÀO server (không dùng trong demo này)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket, SockJS fallback cho browser cũ
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
