import React from 'react';
import { Activity } from 'lucide-react';
import { useWebSocket } from './hooks/useWebSocket';
import { ConnectionForm } from './components/ConnectionForm';
import { StatusIndicator } from './components/StatusIndicator';
import { MessageInput } from './components/MessageInput';
import { MessageList } from './components/MessageList';
import { MessageType } from './types';

/**
 * Main application component
 * Orchestrates WebSocket connection and UI components
 */
function App() {
  const {
    status,
    messages,
    connect,
    disconnect,
    sendMessage,
    clearMessages,
  } = useWebSocket();

  /**
   * Handles connection request from ConnectionForm
   */
  const handleConnect = (host: string, port: number) => {
    connect({
      host,
      port,
      path: '/',
      secure: false,
      pingInterval: 30000, // 30 seconds
      pongTimeout: 5000, // 5 seconds
      autoReconnect: true,
      maxReconnectAttempts: 5,
      reconnectDelay: 3000, // 3 seconds
    });
  };

  /**
   * Handles disconnect request
   */
  const handleDisconnect = () => {
    disconnect();
  };

  /**
   * Handles message send request
   */
  const handleSendMessage = (data: string | ArrayBuffer, type: MessageType) => {
    sendMessage(data, type);
  };

  /**
   * Handles clear messages request
   */
  const handleClearMessages = () => {
    clearMessages();
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <header className="bg-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center gap-3">
            <Activity size={32} className="text-blue-600" />
            <div>
              <h1 className="text-3xl font-bold text-gray-900">
                WebSocket Client
              </h1>
              <p className="text-sm text-gray-600">
                Real-time WebSocket communication with health monitoring
              </p>
            </div>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Left column - Connection and Status */}
          <div className="space-y-6">
            <ConnectionForm
              status={status}
              onConnect={handleConnect}
              onDisconnect={handleDisconnect}
            />
            
            <StatusIndicator status={status} />
            
            <MessageInput
              status={status}
              onSend={handleSendMessage}
            />
          </div>

          {/* Right column - Message History */}
          <div>
            <MessageList
              messages={messages}
              onClear={handleClearMessages}
            />
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="text-center text-sm text-gray-600">
            <p>
              Built with React, TypeScript, and WebSocket API
            </p>
            <p className="mt-1">
              Features: Automatic ping/pong health checks, multiple message types, auto-reconnection
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
