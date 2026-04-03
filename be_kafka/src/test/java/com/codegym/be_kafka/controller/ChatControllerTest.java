package com.codegym.be_kafka.controller;

import com.codegym.be_kafka.service.ChatProducerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatProducerService chatProducerService;

    @Test
    public void testSendValidMessage() throws Exception {
        // Assume service does nothing special, just completes successfully
        Mockito.doNothing().when(chatProducerService).send(anyString(), anyString());

        String jsonPayload = """
                {
                    "userId": "user-test",
                    "content": "Hello World"
                }
                """;

        mockMvc.perform(post("/api/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sent"))
                .andExpect(jsonPath("$.userId").value("user-test"))
                .andExpect(jsonPath("$.content").value("Hello World"));
    }

    @Test
    public void testSendEmptyMessage() throws Exception {
        String jsonPayload = """
                {
                    "userId": "user-test",
                    "content": ""
                }
                """;

        mockMvc.perform(post("/api/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Content is empty"));
    }

    @Test
    public void testGetTopicInfo() throws Exception {
        mockMvc.perform(get("/api/topic-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("chat-topic"))
                .andExpect(jsonPath("$.partitions").value(3));
    }
}
