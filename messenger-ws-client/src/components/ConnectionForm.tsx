import React, { useState } from 'react';
import { Plug, PlugZap } from 'lucide-react';
import { ConnectionStatus } from '../types';

interface ConnectionFormProps {
  /** Current connection status */
  status: ConnectionStatus;
  /** Callback when connect button is clicked */
  onConnect: (host: string, port: number) => void;
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
  const [url, setUrl] = useState('ws://localhost:8080');

  const isConnecting = status === ConnectionStatus.CONNECTING;
  const isConnected = status === ConnectionStatus.CONNECTED;
  const canConnect = !isConnecting && !isConnected;

  /**
   * Parses WebSocket URL and extracts host and port
   */
  const parseWebSocketUrl = (wsUrl: string): { host: string; port: number } | null => {
    try {
      // Add ws:// prefix if not present
      let urlToParse = wsUrl.trim();
      if (!urlToParse.startsWith('ws://') && !urlToParse.startsWith('wss://')) {
        urlToParse = 'ws://' + urlToParse;
      }

      const parsedUrl = new URL(urlToParse);
      
      // Extract host (hostname without port)
      const host = parsedUrl.hostname;
      
      // Extract port (use default if not specified)
      let port = parsedUrl.port ? parseInt(parsedUrl.port, 10) : (parsedUrl.protocol === 'wss:' ? 443 : 80);
      
      // Validate port
      if (isNaN(port) || port < 1 || port > 65535) {
        return null;
      }

      return { host, port };
    } catch (error) {
      return null;
    }
  };

  /**
   * Handles form submission
   */
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (canConnect) {
      const parsed = parseWebSocketUrl(url);
      
      if (!parsed) {
        alert('Please enter a valid WebSocket URL (e.g., ws://localhost:8080 or localhost:8080)');
        return;
      }

      onConnect(parsed.host, parsed.port);
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
