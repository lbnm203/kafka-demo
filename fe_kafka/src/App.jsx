import './App.css';
import { ChatBox }   from './pages/ChatBox';
import { SpamPanel } from './pages/SpamPanel';
import { KafkaLog }  from './pages/KafkaLog';
import { useKafkaWebSocket } from './service/useKafkaWebSocket';

/**
 * App - Layout 2 cột
 * ─────────────────────────────────────────────────────────────
 * LEFT  (420px) : ChatBox (gửi 1 message) + SpamPanel (mass send)
 * RIGHT (flex-1): KafkaLog (real-time consumer log)
 * ─────────────────────────────────────────────────────────────
 */
function App() {
  const { messages, connected, clearMessages } = useKafkaWebSocket(5000);

  return (
    <div className="app">
      {/* Top bar */}
      <header className="topbar">
        <div className="topbar-brand">
          <span className="topbar-logo">⚡</span>
          <span className="topbar-title">Kafka Chat Demo</span>
        </div>
        <div className="topbar-info">
          <span className="chip chip-blue">chat-topic</span>
          <span className="chip chip-green">10 Partitions</span>
          <span className="chip chip-purple">concurrency = 10</span>
          <span className={`chip ${connected ? 'chip-green' : 'chip-red'}`}>
            {connected ? '● WebSocket Live' : '○ WS Offline'}
          </span>
        </div>
      </header>

      {/* Main layout */}
      <main className="main">
        {/* Left column */}
        <aside className="left-col">
          <ChatBox />
          <SpamPanel />
        </aside>

        {/* Right column – stretches full height */}
        <section className="right-col">
          <KafkaLog
            messages={messages}
            connected={connected}
            clearMessages={clearMessages}
          />
        </section>
      </main>
    </div>
  );
}

export default App;
