import React, { useState } from 'react';
import { Plug, PlugZap, Info } from 'lucide-react';
import { ConnectionStatus } from '../types';

interface ConnectionFormProps {
  /** Current connection status */
  status: ConnectionStatus;
  /** Callback when connect button is clicked */
  onConnect: (url: string, multiplexing: boolean) => void;
  /** Callback when disconnect button is clicked */
  onDisconnect: () => void;
}

/**
 * Form component for managing WebSocket connection
 * Allows user to input full WebSocket URL and connect/disconnect
 */
export const ConnectionForm: React.FC<ConnectionFormProps> = ({
  status,
  onConnect,
  onDisconnect,
}) => {
  const [url, setUrl] = useState('ws://localhost:7777/api/v1/chat/ws');
  const [multiplexing, setMultiplexing] = useState(false);
  const [showInfo, setShowInfo] = useState(false);

  const isConnecting = status === ConnectionStatus.CONNECTING;
  const isConnected = status === ConnectionStatus.CONNECTED;
  const canConnect = !isConnecting && !isConnected;

  /**
   * Handles form submission
   */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (canConnect) {
      if (!url.trim()) {
        alert('Please enter a valid WebSocket URL (e.g., ws://localhost:8080/path)');
        return;
      }
      onConnect(url.trim(), multiplexing);
    }
  };

  /**
   * Handles disconnect button click
   */
  const handleDisconnect = () => {
    onDisconnect();
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">Connection Settings</h2>
      
      <div className="space-y-4">
        {/* WebSocket URL input */}
        <div>
          <label htmlFor="url" className="block text-sm font-medium text-gray-700 mb-1">
            WebSocket URL
          </label>
          <input
            type="text"
            id="url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            disabled={!canConnect}
            placeholder="ws://localhost:8080 or localhost:8080"
            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed font-mono text-sm"
            required
          />
          <p className="mt-1 text-xs text-gray-500">
            Examples: ws://localhost:8080, wss://example.com:443, 192.168.1.100:7777
          </p>
        </div>

        {/* Multiplexing Extension */}
        <div className="border-t border-gray-200 pt-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="multiplexing"
                checked={multiplexing}
                onChange={(e) => setMultiplexing(e.target.checked)}
                disabled={!canConnect}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500 disabled:cursor-not-allowed"
              />
              <label htmlFor="multiplexing" className="text-sm font-medium text-gray-700">
                Enable Multiplexing Extension
              </label>
            </div>
            <button
              type="button"
              onMouseEnter={() => setShowInfo(true)}
              onMouseLeave={() => setShowInfo(false)}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <Info size={18} />
            </button>
          </div>
          
          {/* Info tooltip */}
          {showInfo && (
            <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-md text-sm text-gray-700">
              <p className="font-semibold mb-1">Multiplexing Extension</p>
              <p className="mb-2">Enables advanced WebSocket protocol with:</p>
              <ul className="list-disc list-inside space-y-1 text-xs">
                <li>Message framing with unique IDs</li>
                <li>Support for single and multi-frame messages</li>
                <li>Advanced message assembly and ordering</li>
              </ul>
              <p className="mt-2 text-xs italic">
                Technical: Sends <code className="bg-white px-1 py-0.5 rounded">Sec-WebSocket-Protocol: multiplexing</code> header during handshake
              </p>
            </div>
          )}
        </div>

        {/* Connect/Disconnect buttons */}
        <div className="flex gap-3">
          {!isConnected ? (
            <button
              type="submit"
              disabled={isConnecting}
              className="flex-1 flex items-center justify-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors font-medium"
            >
              <Plug size={20} />
              {isConnecting ? 'Connecting...' : 'Connect'}
            </button>
          ) : (
            <button
              type="button"
              onClick={handleDisconnect}
              className="flex-1 flex items-center justify-center gap-2 bg-red-600 text-white px-6 py-3 rounded-md hover:bg-red-700 transition-colors font-medium"
            >
              <PlugZap size={20} />
              Disconnect
            </button>
          )}
        </div>
      </div>
    </form>
  );
};
