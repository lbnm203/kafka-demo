import { useState } from 'react';
import { triggerSpam } from '../service/api';
import styles from './SpamPanel.module.css';

/**
 * SpamPanel - Demo đồng thời
 * Gọi /api/spam để giả lập N user × M message gửi cùng lúc.
 * Xem log bên phải để thấy partition routing và race condition (hay không có).
 */
export function SpamPanel() {
  const [users, setUsers] = useState(10);
  const [msgsPerUser, setMsgsPerUser] = useState(50);
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState(null);

  const handleSpam = async () => {
    setRunning(true);
    setResult(null);
    try {
      const res = await triggerSpam(users, msgsPerUser);
      setResult(res.data);
    } catch (e) {
      setResult({ error: e.message });
    } finally {
      setRunning(false);
    }
  };

  return (
    <div className={styles.panel}>
      <div className={styles.header}>
        <span className={styles.icon}>⚡</span>
        <h2>Concurrency Demo</h2>
        <span className={styles.badge}>Spam</span>
      </div>

      <p className={styles.desc}>
        Giả lập nhiều user gửi message <strong>cùng lúc</strong>.<br />
        Quan sát log để hiểu Kafka phân phối ra sao.
      </p>

      <div className={styles.controls}>
        <div className={styles.control}>
          <label>Users</label>
          <input
            type="number" min={1} max={20}
            value={users}
            onChange={e => setUsers(Number(e.target.value))}
            className={styles.numInput}
            id="input-users"
          />
        </div>
        <div className={styles.control}>
          <label>Msgs / User</label>
          <input
            type="number" min={1} max={500}
            value={msgsPerUser}
            onChange={e => setMsgsPerUser(Number(e.target.value))}
            className={styles.numInput}
            id="input-msgs-per-user"
          />
        </div>
      </div>

      <div className={styles.total}>
        Total: <strong>{(users * msgsPerUser).toLocaleString()}</strong> messages
      </div>

      <button
        onClick={handleSpam}
        disabled={running}
        className={styles.spamBtn}
        id="btn-spam"
      >
        {running ? '🚀 Spamming…' : '⚡ Start Spam'}
      </button>

      {
        result && !result.error && (
          <div className={styles.result}>
            <div className={styles.resultRow}>
              <span>Status</span><span className={styles.ok}>{result.status}</span>
            </div>
            <div className={styles.resultRow}>
              <span>Users</span><span>{result.users}</span>
            </div>
            <div className={styles.resultRow}>
              <span>Msgs/User</span><span>{result.messagesPerUser}</span>
            </div>
            <div className={styles.resultRow}>
              <span>Total</span><span className={styles.highlight}>{result.totalMessages?.toLocaleString()}</span>
            </div>
          </div>
        )
      }

      {
        result?.error && (
          <div className={styles.error}>{result.error}</div>
        )
      }

      {/* Explaining what to look for */}
      <div className={styles.explainBox}>
        <div className={styles.explainTitle}>🔍 Quan sát trong Log Panel:</div>
        <ul className={styles.explainList}>
          <li>Cùng userId → cùng partition <em>luôn luôn</em></li>
          <li>Offset tăng dần trong mỗi partition</li>
          <li>Khác partition → chạy song song (khác thread)</li>
          <li><strong>Không có race condition</strong> vì 1 partition = 1 thread</li>
        </ul>
      </div>
    </div >
  );
}
