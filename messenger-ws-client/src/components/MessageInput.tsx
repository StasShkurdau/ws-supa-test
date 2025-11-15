import React, { useState } from 'react';
import { Send, FileText, Binary } from 'lucide-react';
import { MessageType, ConnectionStatus } from '../types';

interface MessageInputProps {
  /** Current connection status */
  status: ConnectionStatus;
  /** Callback when send button is clicked */
  onSend: (data: string | ArrayBuffer, type: MessageType) => void;
}

/**
 * Component for composing and sending messages
 * Supports both TEXT and BINARY message types
 */
export const MessageInput: React.FC<MessageInputProps> = ({ status, onSend }) => {
  const [messageText, setMessageText] = useState('');
  const [messageType, setMessageType] = useState<MessageType>(MessageType.TEXT);
  
  const isConnected = status === ConnectionStatus.CONNECTED;

  /**
   * Handles form submission
   */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!messageText.trim() || !isConnected) {
      return;
    }

    // Convert message based on selected type
    if (messageType === MessageType.BINARY) {
      // Convert text to binary (ArrayBuffer)
      const encoder = new TextEncoder();
      const data = encoder.encode(messageText);
      onSend(data.buffer, MessageType.BINARY);
    } else {
      // Send as text
      onSend(messageText, MessageType.TEXT);
    }

    // Clear input after sending
    setMessageText('');
  };

  /**
   * Handles file selection for binary messages
   */
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !isConnected) {
      return;
    }

    // Read file as ArrayBuffer
    const reader = new FileReader();
    reader.onload = (event) => {
      const arrayBuffer = event.target?.result as ArrayBuffer;
      if (arrayBuffer) {
        onSend(arrayBuffer, MessageType.BINARY);
      }
    };
    reader.readAsArrayBuffer(file);

    // Reset file input
    e.target.value = '';
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">Send Message</h2>
      
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Message type selector */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Message Type
          </label>
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => setMessageType(MessageType.TEXT)}
              disabled={!isConnected}
              className={`flex-1 flex items-center justify-center gap-2 px-4 py-2 rounded-md border-2 transition-colors ${
                messageType === MessageType.TEXT
                  ? 'border-blue-600 bg-blue-50 text-blue-700'
                  : 'border-gray-300 bg-white text-gray-700 hover:border-gray-400'
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              <FileText size={20} />
              TEXT
            </button>
            <button
              type="button"
              onClick={() => setMessageType(MessageType.BINARY)}
              disabled={!isConnected}
              className={`flex-1 flex items-center justify-center gap-2 px-4 py-2 rounded-md border-2 transition-colors ${
                messageType === MessageType.BINARY
                  ? 'border-blue-600 bg-blue-50 text-blue-700'
                  : 'border-gray-300 bg-white text-gray-700 hover:border-gray-400'
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              <Binary size={20} />
              BINARY
            </button>
          </div>
        </div>

        {/* Message input */}
        <div>
          <label htmlFor="message" className="block text-sm font-medium text-gray-700 mb-1">
            Message Content
          </label>
          <textarea
            id="message"
            value={messageText}
            onChange={(e) => setMessageText(e.target.value)}
            disabled={!isConnected}
            placeholder={
              isConnected
                ? messageType === MessageType.TEXT
                  ? 'Enter your text message...'
                  : 'Enter text to send as binary data...'
                : 'Connect to server to send messages'
            }
            rows={4}
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed resize-none"
          />
        </div>

        {/* Action buttons */}
        <div className="flex gap-3">
          <button
            type="submit"
            disabled={!isConnected || !messageText.trim()}
            className="flex-1 flex items-center justify-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors font-medium"
          >
            <Send size={20} />
            Send
          </button>

          {/* File upload for binary messages */}
          {messageType === MessageType.BINARY && (
            <label className="flex-1">
              <input
                type="file"
                onChange={handleFileSelect}
                disabled={!isConnected}
                className="hidden"
              />
              <div className="flex items-center justify-center gap-2 bg-purple-600 text-white px-6 py-3 rounded-md hover:bg-purple-700 disabled:bg-gray-400 cursor-pointer transition-colors font-medium">
                <Binary size={20} />
                Send File
              </div>
            </label>
          )}
        </div>

        {/* Help text */}
        {!isConnected && (
          <p className="text-sm text-gray-500 text-center">
            Please connect to a WebSocket server to send messages
          </p>
        )}
      </form>
    </div>
  );
};
