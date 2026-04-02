package com.codegym.be_kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Đây là dữ liệu mà CONSUMER nhận được và broadcast ra WebSocket.
 * Chứa đủ thông tin để hiểu Kafka xử lý message ra sao:
 *   - partition: message nằm ở partition nào
 *   - offset   : vị trí của message trong partition đó
 *   - thread   : thread nào trong consumer group xử lý
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsumedMessage {
    private String userId;
    private String content;
    private int    partition;
    private long   offset;
    private String thread;
    private long   timestamp;
}
