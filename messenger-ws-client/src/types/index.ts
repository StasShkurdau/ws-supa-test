/**
 * Represents the current status of the WebSocket connection
 */
export enum ConnectionStatus {
  /** Connection has not been initiated yet */
  NOT_STARTED = 'NOT_STARTED',
  /** Currently attempting to establish connection */
  CONNECTING = 'CONNECTING',
  /** Successfully connected to the server */
  CONNECTED = 'CONNECTED',
  /** Connection is being closed */
  DISCONNECTING = 'DISCONNECTING',
  /** Connection has been closed normally */
  DISCONNECTED = 'DISCONNECTED',
  /** Failed to establish connection (network unreachable) */
  NO_ROUTE_TO_HOST = 'NO_ROUTE_TO_HOST',
  /** Connection error occurred */
  ERROR = 'ERROR',
  /** Connection lost, attempting to reconnect */
  RECONNECTING = 'RECONNECTING',
}

/**
 * Types of WebSocket message frames
 * Corresponds to WebSocket protocol frame types
 */
export enum MessageType {
  /** Text frame - UTF-8 encoded string data */
  TEXT = 'TEXT',
  /** Binary frame - raw binary data */
  BINARY = 'BINARY',
  /** Ping frame - used for connection health checks */
  PING = 'PING',
  /** Pong frame - response to ping */
  PONG = 'PONG',
  /** Close frame - connection termination */
  CLOSE = 'CLOSE',
}

/**
 * Represents a message in the WebSocket communication
 */
export interface Message {
  /** Unique identifier for the message */
  id: string;
  /** Type of the message frame */
  type: MessageType;
  /** Message content (string for TEXT, ArrayBuffer for BINARY) */
  data: string | ArrayBuffer;
  /** Timestamp when the message was created */
  timestamp: Date;
  /** Direction of the message */
  direction: 'sent' | 'received';
}

/**
 * Configuration for WebSocket connection
 */
export interface WebSocketConfig {
  /** Server hostname or IP address */
  host: string;
  /** Server port number */
  port: number;
  /** Optional path for WebSocket endpoint (default: '/') */
  path?: string;
  /** Use secure WebSocket (wss://) instead of ws:// */
  secure?: boolean;
  /** Enable multiplexing extension for advanced message handling */
  multiplexing?: boolean;
  /** Interval for ping health checks in milliseconds (default: 30000) */
  pingInterval?: number;
  /** Timeout for pong response in milliseconds (default: 5000) */
  pongTimeout?: number;
  /** Enable automatic reconnection on connection loss */
  autoReconnect?: boolean;
  /** Maximum number of reconnection attempts (default: 5) */
  maxReconnectAttempts?: number;
  /** Delay between reconnection attempts in milliseconds (default: 3000) */
  reconnectDelay?: number;
}

/**
 * WebSocket client event handlers
 */
export interface WebSocketEventHandlers {
  /** Called when connection status changes */
  onStatusChange?: (status: ConnectionStatus) => void;
  /** Called when a message is received */
  onMessage?: (message: Message) => void;
  /** Called when an error occurs */
  onError?: (error: Error) => void;
}
