import { useState, useEffect, useCallback, useRef } from 'react';
import { WebSocketClient } from '../services/WebSocketClient';
import {
  ConnectionStatus,
  MessageType,
  Message,
} from '../types';

/**
 * Custom React hook for managing WebSocket connection
 * Provides state management and methods for WebSocket operations
 */
export const useWebSocket = () => {
  const [status, setStatus] = useState<ConnectionStatus>(ConnectionStatus.NOT_STARTED);
  const [messages, setMessages] = useState<Message[]>([]);
  const [error, setError] = useState<Error | null>(null);
  
  // Use ref to maintain WebSocket client instance across re-renders
  const clientRef = useRef<WebSocketClient | null>(null);

  /**
   * Connects to WebSocket server with provided configuration
   */
  const connect = useCallback((config: any) => {
    // Disconnect existing connection if any
    if (clientRef.current) {
      clientRef.current.disconnect();
    }

    // If config.url is present, parse it and add multiplexing if needed
    if (typeof config.url === 'string') {
      // Parse URL to extract components
      let url = config.url;
      const multiplexing = config.multiplexing || false;
      
      // Remove protocol if present to parse
      let protocol = 'ws';
      let hostPath = url;
      
      if (url.startsWith('wss://')) {
        protocol = 'wss';
        hostPath = url.substring(6);
      } else if (url.startsWith('ws://')) {
        protocol = 'ws';
        hostPath = url.substring(5);
      }
      
      // Split host:port and path
      const firstSlash = hostPath.indexOf('/');
      let hostPort = hostPath;
      let path = '/';
      
      if (firstSlash !== -1) {
        hostPort = hostPath.substring(0, firstSlash);
        path = hostPath.substring(firstSlash);
      }
      
      // Split host and port
      const [host, portStr] = hostPort.split(':');
      const port = portStr ? parseInt(portStr, 10) : (protocol === 'wss' ? 443 : 80);
      
      clientRef.current = new WebSocketClient({
        host,
        port,
        path,
        secure: protocol === 'wss',
        multiplexing,
      }, {
        onStatusChange: (newStatus) => setStatus(newStatus),
        onMessage: (message) => setMessages((prev) => [...prev, message]),
        onError: (err) => setError(err),
      });
    } else {
      if (typeof config.url === 'string') {
        clientRef.current = new WebSocketClient({ url: config.url }, {
          onStatusChange: (newStatus) => setStatus(newStatus),
          onMessage: (message) => setMessages((prev) => [...prev, message]),
          onError: (err) => setError(err),
        });
      } else {
        clientRef.current = new WebSocketClient(config, {
          onStatusChange: (newStatus) => setStatus(newStatus),
          onMessage: (message) => setMessages((prev) => [...prev, message]),
          onError: (err) => setError(err),
        });
      }
    }

    // Initiate connection (user-initiated)
    clientRef.current.connectUserInitiated();
  }, []);

  /**
   * Disconnects from WebSocket server
   */
  const disconnect = useCallback(() => {
    if (clientRef.current) {
      clientRef.current.disconnect();
      clientRef.current = null;
    }
  }, []);

  /**
   * Sends a message through WebSocket connection
   */
  const sendMessage = useCallback((
    data: string | ArrayBuffer,
    type: MessageType = MessageType.TEXT
  ): boolean => {
    if (!clientRef.current) {
      console.error('WebSocket client not initialized');
      return false;
    }

    return clientRef.current.send(data, type);
  }, []);

  /**
   * Clears all messages from the message list
   */
  const clearMessages = useCallback(() => {
    setMessages([]);
  }, []);

  /**
   * Clears the error state
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  /**
   * Gets the current connection status
   */
  const getStatus = useCallback((): ConnectionStatus => {
    return clientRef.current?.getStatus() || ConnectionStatus.NOT_STARTED;
  }, []);

  /**
   * Checks if WebSocket is currently connected
   */
  const isConnected = useCallback((): boolean => {
    return clientRef.current?.isConnected() || false;
  }, []);

  /**
   * Cleanup on component unmount
   */
  useEffect(() => {
    return () => {
      if (clientRef.current) {
        clientRef.current.disconnect();
      }
    };
  }, []);

  return {
    status,
    messages,
    error,
    connect,
    disconnect,
    sendMessage,
    clearMessages,
    clearError,
    getStatus,
    isConnected,
  };
};
