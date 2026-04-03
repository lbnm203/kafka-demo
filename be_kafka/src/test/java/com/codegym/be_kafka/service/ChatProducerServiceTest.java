package com.codegym.be_kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ChatProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatProducerService chatProducerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendMessageSuccessfully() throws Exception {
        String userId = "test-user";
        String content = "Hello!";
        String mockJson = "{\"userId\":\"test-user\",\"content\":\"Hello!\"}";

        when(objectMapper.writeValueAsString(any())).thenReturn(mockJson);

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        // Note: we won't fully complete it with a mocked SendResult here because it involves internal Kafka classes, 
        // but returning an incomplete future is enough to ensure send is called.
        when(kafkaTemplate.send(anyString(), eq(userId), eq(mockJson))).thenReturn(future);

        chatProducerService.send(userId, content);

        verify(kafkaTemplate, times(1)).send(anyString(), eq(userId), eq(mockJson));
    }

    @Test
    public void testSendMessageWithJsonException() throws Exception {
        String userId = "test-user";
        String content = "Hello!";

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mock error") {});

        chatProducerService.send(userId, content);

        // Verify kafkaTemplate.send is NEVER called because JSON conversion failed
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }
}
