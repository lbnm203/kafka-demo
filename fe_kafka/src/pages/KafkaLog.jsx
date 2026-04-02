import { useRef, useEffect } from 'react';
import styles from './KafkaLog.module.css';

// Màu partition: P0=blue, P1=green, P2=orange (match CSS vars)
// Màu partition: P0-P2 có màu riêng, P3-P9 dùng màu chung hoặc xoay vòng
const PARTITION_COLORS = ['p0', 'p1', 'p2', 'p3', 'p4', 'p5', 'p0', 'p1', 'p2', 'p3'];

/**
 * KafkaLog - Panel bên PHẢI
 * Hiển thị real-time các ConsumedMessage từ WebSocket.
 */
export function KafkaLog({ messages, connected, clearMessages }) {
  const bottomRef = useRef(null);

  // Auto-scroll xuống cuối khi có message mới
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Tạo mảng stats cho 10 partitions (0-9)
  const partitionCounts = Array.from({ length: 10 }, (_, i) => 
    messages.filter(m => m.partition === i).length
  );
  const total = messages.length;

  return (
    <div className={styles.panel}>
      {/* Header */}
      <div className={styles.header}>
        <span className={styles.icon}>📊</span>
        <h2>Kafka Consumer Log</h2>
        <div
          className={`${styles.wsStatus} ${connected ? styles.connected : styles.disconnected}`}
          title={connected ? 'WebSocket connected' : 'WebSocket disconnected'}
        >
          <span className={styles.dot} />
          {connected ? 'Live' : 'Offline'}
        </div>
      </div>

      {/* Partition stats (Grid 5x2 hoặc scrollable) */}
      <div className={styles.stats}>
        {partitionCounts.map((count, p) => (
          <div key={p} className={`${styles.stat} ${styles[PARTITION_COLORS[p]] || styles.p0}`}>
            <div className={styles.statLabel}>P{p}</div>
            <div className={styles.statVal}>{count}</div>
          </div>
        ))}
        <div className={styles.stat}>
          <div className={styles.statLabel}>Total</div>
          <div className={styles.statVal}>{total}</div>
        </div>
      </div>

      {/* Actions */}
      <div className={styles.actions}>
        <button onClick={clearMessages} className={styles.clearBtn} id="btn-clear-log">
          Clear
        </button>
        <span className={styles.hint}>
          Cùng userId → cùng partition. Offset tăng dần.
        </span>
      </div>

      {/* Log lines */}
      <div className={styles.logArea} id="kafka-log-area">
        {messages.length === 0 && (
          <div className={styles.empty}>
            {connected
              ? 'Waiting for messages… Send one or click Spam!'
              : '⚠ WebSocket not connected. Is the backend running?'}
          </div>
        )}
        {messages.map((msg) => {
          const pColor = PARTITION_COLORS[msg.partition] || 'p0';
          const pLabel = `P${msg.partition}`;
          // Sử dụng partition-offset làm key duy nhất (từ Kafka)
          const uniqueKey = `${msg.partition}-${msg.offset}`;
          return (
            <div key={uniqueKey} className={styles.logLine}>
              <span className={`${styles.pBadge} ${styles[pColor]}`}>{pLabel}</span>
              <span className={styles.offset}>@{msg.offset}</span>
              <span className={styles.userId}>{msg.userId}</span>
              <span className={styles.content}>{msg.content}</span>
              <span className={styles.thread} title={msg.thread}>
                {(msg.thread || '').replace('org.springframework.kafka.KafkaListenerEndpointContainer#', 'container#')}
              </span>
            </div>
          );
        })}
        <div ref={bottomRef} />
      </div>
    </div>
  );
}
