import React, { useEffect, useRef } from 'react';
import { ArrowUpCircle, ArrowDownCircle, Trash2 } from 'lucide-react';
import { Message, MessageType } from '../types';

interface MessageListProps {
  /** Array of messages to display */
  messages: Message[];
  /** Callback to clear all messages */
  onClear: () => void;
}

/**
 * Component for displaying message history
 * Shows sent and received messages with timestamps and types
 */
export const MessageList: React.FC<MessageListProps> = ({ messages, onClear }) => {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  /**
   * Auto-scroll to bottom when new messages arrive
   */
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  /**
   * Formats message data for display
   */
  const formatMessageData = (message: Message): string => {
    if (message.type === MessageType.BINARY) {
      if (message.data instanceof ArrayBuffer) {
        // Display binary data as hex string (first 100 bytes)
        const bytes = new Uint8Array(message.data);
        const hexString = Array.from(bytes.slice(0, 100))
          .map(b => b.toString(16).padStart(2, '0'))
          .join(' ');
        const suffix = bytes.length > 100 ? '...' : '';
        return `[Binary ${bytes.length} bytes] ${hexString}${suffix}`;
      }
    }
    
    return String(message.data);
  };

  /**
   * Formats timestamp for display
   */
  const formatTimestamp = (date: Date): string => {
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    });
  };

  /**
   * Gets CSS classes for message type badge
   */
  const getMessageTypeBadge = (type: MessageType): string => {
    switch (type) {
      case MessageType.TEXT:
        return 'bg-blue-100 text-blue-800';
      case MessageType.BINARY:
        return 'bg-purple-100 text-purple-800';
      case MessageType.PING:
        return 'bg-green-100 text-green-800';
      case MessageType.PONG:
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-bold text-gray-800">Message History</h2>
        {messages.length > 0 && (
          <button
            onClick={onClear}
            className="flex items-center gap-2 px-4 py-2 text-sm bg-red-100 text-red-700 rounded-md hover:bg-red-200 transition-colors"
          >
            <Trash2 size={16} />
            Clear
          </button>
        )}
      </div>

      {/* Messages container */}
      <div className="flex-1 overflow-y-auto space-y-3 min-h-[400px] max-h-[600px]">
        {messages.length === 0 ? (
          <div className="flex items-center justify-center h-full text-gray-400">
            <p className="text-center">
              No messages yet<br />
              <span className="text-sm">Messages will appear here when sent or received</span>
            </p>
          </div>
        ) : (
          messages.map((message) => (
            <div
              key={message.id}
              className={`p-4 rounded-lg border-l-4 ${
                message.direction === 'sent'
                  ? 'bg-blue-50 border-blue-500'
                  : 'bg-green-50 border-green-500'
              }`}
            >
              {/* Message header */}
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  {message.direction === 'sent' ? (
                    <ArrowUpCircle size={18} className="text-blue-600" />
                  ) : (
                    <ArrowDownCircle size={18} className="text-green-600" />
                  )}
                  <span className="font-semibold text-gray-800">
                    {message.direction === 'sent' ? 'Sent' : 'Received'}
                  </span>
                  <span className={`px-2 py-1 rounded text-xs font-medium ${getMessageTypeBadge(message.type)}`}>
                    {message.type}
                  </span>
                </div>
                <span className="text-xs text-gray-500">
                  {formatTimestamp(message.timestamp)}
                </span>
              </div>

              {/* Message content */}
              <div className="bg-white p-3 rounded border border-gray-200">
                <pre className="text-sm text-gray-700 whitespace-pre-wrap break-words font-mono">
                  {formatMessageData(message)}
                </pre>
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>
    </div>
  );
};
