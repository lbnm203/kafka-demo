import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * Hook quản lý kết nối WebSocket tới backend.
 * Tự động reconnect khi mất kết nối.
 *
 * @returns {messages, connected}
 *   messages  - mảng ConsumedMessage nhận từ /topic/messages
 *   connected - trạng thái kết nối WS
 */
export function useKafkaWebSocket(maxMessages = 500) {
  const [messages, setMessages]   = useState([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8081/ws'),
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/messages', (frame) => {
          const msg = JSON.parse(frame.body);
          setMessages(prev => {
            const next = [...prev, { ...msg, id: Date.now() + Math.random() }];
            // Giữ tối đa maxMessages để tránh memory leak khi spam
            return next.length > maxMessages ? next.slice(-maxMessages) : next;
          });
        });
      },
      onDisconnect: () => setConnected(false),
      onStompError: (frame) => {
        console.error('STOMP error', frame);
        setConnected(false);
      }
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [maxMessages]);

  const clearMessages = () => setMessages([]);

  return { messages, connected, clearMessages };
}
