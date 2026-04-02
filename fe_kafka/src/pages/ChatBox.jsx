import { useState, useRef, useEffect } from 'react';
import { sendMessage } from '../service/api';
import styles from './ChatBox.module.css';

/**
 * ChatBox - Panel bên TRÁI
 * Cho phép user nhập userId + content và gửi message qua /api/send
 */
export function ChatBox() {
  const [userId, setUserId]   = useState('user-1');
  const [content, setContent] = useState('');
  const [status, setStatus]   = useState(null);   // {type: 'ok'|'err', text}
  const [sending, setSending] = useState(false);

  const handleSend = async () => {
    if (!content.trim()) return;
    setSending(true);
    setStatus(null);
    try {
      await sendMessage(userId, content.trim());
      setStatus({ type: 'ok', text: `✓ Sent to Kafka` });
      setContent('');
    } catch (e) {
      const serverMsg = e.response?.data?.message || e.message;
      setStatus({ type: 'err', text: `✗ Error: ${serverMsg}` });
    } finally {
      setSending(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className={styles.chatBox}>
      <div className={styles.header}>
        <span className={styles.icon}>💬</span>
        <h2>Send Message</h2>
        <span className={styles.badge}>Producer</span>
      </div>

      <div className={styles.field}>
        <label>User ID</label>
        <select
          value={userId}
          onChange={e => setUserId(e.target.value)}
          className={styles.select}
        >
          {Array.from({ length: 10 }, (_, i) => (
            <option key={i} value={`user-${i + 1}`}>user-{i + 1}</option>
          ))}
        </select>
        <p className={styles.hint}>
          Kafka dùng userId làm key → hash(userId) % 3 → partition cố định
        </p>
      </div>

      <div className={styles.field}>
        <label>Message</label>
        <textarea
          rows={3}
          placeholder="Type your message..."
          value={content}
          onChange={e => setContent(e.target.value)}
          onKeyDown={handleKeyDown}
          className={styles.textarea}
        />
      </div>

      <button
        onClick={handleSend}
        disabled={sending || !content.trim()}
        className={styles.sendBtn}
        id="btn-send-message"
      >
        {sending ? 'Sending…' : 'Send to Kafka →'}
      </button>

      {status && (
        <div className={`${styles.status} ${styles[status.type]}`}>
          {status.text}
        </div>
      )}

      {/* Flow diagram */}
      <div className={styles.flow}>
        <div className={styles.flowStep}>Frontend</div>
        <div className={styles.flowArrow}>→</div>
        <div className={styles.flowStep}>POST /api/send</div>
        <div className={styles.flowArrow}>→</div>
        <div className={styles.flowStep}>Producer</div>
        <div className={styles.flowArrow}>→</div>
        <div className={styles.flowStep}>Kafka</div>
        <div className={styles.flowArrow}>→</div>
        <div className={styles.flowStep}>Consumer</div>
        <div className={styles.flowArrow}>→</div>
        <div className={styles.flowStep}>WebSocket</div>
        <div className={styles.flowArrow}>→</div>
        <div className={styles.flowStep}>Log Panel</div>
      </div>
    </div>
  );
}
