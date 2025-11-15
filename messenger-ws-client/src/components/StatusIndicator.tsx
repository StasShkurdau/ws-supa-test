import React from 'react';
import { 
  Circle, 
  CheckCircle2, 
  XCircle, 
  AlertCircle, 
  Loader2,
  WifiOff 
} from 'lucide-react';
import { ConnectionStatus } from '../types';

interface StatusIndicatorProps {
  /** Current connection status */
  status: ConnectionStatus;
}

/**
 * Visual indicator for WebSocket connection status
 * Displays appropriate icon and color based on current status
 */
export const StatusIndicator: React.FC<StatusIndicatorProps> = ({ status }) => {
  /**
   * Gets the appropriate styling and icon for the current status
   */
  const getStatusDisplay = () => {
    switch (status) {
      case ConnectionStatus.NOT_STARTED:
        return {
          icon: <Circle size={24} />,
          color: 'text-gray-400',
          bgColor: 'bg-gray-100',
          label: 'Not Started',
          description: 'Connection has not been initiated',
        };
      
      case ConnectionStatus.CONNECTING:
        return {
          icon: <Loader2 size={24} className="animate-spin" />,
          color: 'text-blue-600',
          bgColor: 'bg-blue-100',
          label: 'Connecting',
          description: 'Establishing connection to server...',
        };
      
      case ConnectionStatus.CONNECTED:
        return {
          icon: <CheckCircle2 size={24} />,
          color: 'text-green-600',
          bgColor: 'bg-green-100',
          label: 'Connected',
          description: 'Successfully connected to server',
        };
      
      case ConnectionStatus.DISCONNECTING:
        return {
          icon: <Loader2 size={24} className="animate-spin" />,
          color: 'text-orange-600',
          bgColor: 'bg-orange-100',
          label: 'Disconnecting',
          description: 'Closing connection...',
        };
      
      case ConnectionStatus.DISCONNECTED:
        return {
          icon: <Circle size={24} />,
          color: 'text-gray-600',
          bgColor: 'bg-gray-100',
          label: 'Disconnected',
          description: 'Connection closed',
        };
      
      case ConnectionStatus.NO_ROUTE_TO_HOST:
        return {
          icon: <WifiOff size={24} />,
          color: 'text-red-600',
          bgColor: 'bg-red-100',
          label: 'No Route to Host',
          description: 'Cannot reach the server - check address and network',
        };
      
      case ConnectionStatus.ERROR:
        return {
          icon: <XCircle size={24} />,
          color: 'text-red-600',
          bgColor: 'bg-red-100',
          label: 'Error',
          description: 'Connection error occurred',
        };
      
      case ConnectionStatus.RECONNECTING:
        return {
          icon: <AlertCircle size={24} />,
          color: 'text-yellow-600',
          bgColor: 'bg-yellow-100',
          label: 'Reconnecting',
          description: 'Attempting to reconnect...',
        };
      
      default:
        return {
          icon: <Circle size={24} />,
          color: 'text-gray-400',
          bgColor: 'bg-gray-100',
          label: 'Unknown',
          description: 'Unknown status',
        };
    }
  };

  const display = getStatusDisplay();

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-2xl font-bold mb-4 text-gray-800">Connection Status</h2>
      
      <div className="flex items-center gap-4">
        {/* Status icon */}
        <div className={`${display.bgColor} ${display.color} p-3 rounded-full`}>
          {display.icon}
        </div>
        
        {/* Status text */}
        <div className="flex-1">
          <div className={`text-xl font-semibold ${display.color}`}>
            {display.label}
          </div>
          <div className="text-sm text-gray-600 mt-1">
            {display.description}
          </div>
        </div>
      </div>
    </div>
  );
};
