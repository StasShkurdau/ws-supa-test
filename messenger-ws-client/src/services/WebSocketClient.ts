import {
  ConnectionStatus,
  MessageType,
  Message,
  WebSocketConfig,
  WebSocketEventHandlers,
} from '../types';

/**
 * WebSocket client with automatic health checks and reconnection support
 * Implements ping/pong mechanism to monitor connection health
 */
export class WebSocketClient {
  private ws: WebSocket | null = null;
  private config: Required<WebSocketConfig>;
  private eventHandlers: WebSocketEventHandlers;
  private currentStatus: ConnectionStatus = ConnectionStatus.NOT_STARTED;
  
  // Health check timers
  private pingIntervalId: number | null = null;
  private pongTimeoutId: number | null = null;
  private lastPongReceived: number = 0;
  
  // Reconnection state
  private reconnectAttempts: number = 0;
  private reconnectTimeoutId: number | null = null;
  private isManualDisconnect: boolean = false;

  /**
   * Creates a new WebSocket client instance
   * @param config - WebSocket connection configuration
   * @param eventHandlers - Event handlers for connection lifecycle
   */
  constructor(config: WebSocketConfig | { url: string }, eventHandlers: WebSocketEventHandlers = {}) {
    if ('url' in config) {
      // Direct URL mode: use as-is
      this.config = {} as any;
      (this.config as any).url = config.url;
    } else {
      // Old config mode
      this.config = {
        host: config.host,
        port: config.port,
        path: config.path || '/',
        secure: config.secure || false,
        pingInterval: config.pingInterval || 30000,
        pongTimeout: config.pongTimeout || 5000,
        autoReconnect: config.autoReconnect !== undefined ? config.autoReconnect : true,
        maxReconnectAttempts: config.maxReconnectAttempts || 5,
        reconnectDelay: config.reconnectDelay || 3000,
      };
    }
    this.eventHandlers = eventHandlers;
  }

  /**
   * Establishes WebSocket connection to the server
   */
  public connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.warn('WebSocket is already connected');
      return;
    }

    this.isManualDisconnect = false;
    this.updateStatus(ConnectionStatus.CONNECTING);

    try {
      let url = '';
      if ('url' in this.config && typeof (this.config as any).url === 'string') {
        url = (this.config as any).url;
      } else {
        const protocol = this.config.secure ? 'wss' : 'ws';
        url = `${protocol}://${this.config.host}:${this.config.port}${this.config.path}`;
      }
      console.log(`Connecting to WebSocket server: ${url}`);
      this.ws = new WebSocket(url);

      // Set up event listeners
      this.ws.onopen = this.handleOpen.bind(this);
      this.ws.onmessage = this.handleMessage.bind(this);
      this.ws.onerror = this.handleError.bind(this);
      this.ws.onclose = this.handleClose.bind(this);
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      this.updateStatus(ConnectionStatus.NO_ROUTE_TO_HOST);
      this.handleReconnection();
    }
  }

  /**
   * Closes the WebSocket connection
   * @param code - WebSocket close code (default: 1000 - normal closure)
   * @param reason - Human-readable close reason
   */
  public disconnect(code: number = 1000, reason: string = 'Client disconnect'): void {
    this.isManualDisconnect = true;
    this.stopHealthCheck();
    this.clearReconnectTimeout();

    if (this.ws) {
      this.updateStatus(ConnectionStatus.DISCONNECTING);
      
      try {
        this.ws.close(code, reason);
      } catch (error) {
        console.error('Error closing WebSocket:', error);
      }
      
      this.ws = null;
    }

    this.updateStatus(ConnectionStatus.DISCONNECTED);
  }

  /**
   * Sends a message through the WebSocket connection
   * @param data - Message data (string for TEXT, ArrayBuffer for BINARY)
   * @param type - Type of message frame (default: TEXT)
   * @returns true if message was sent successfully, false otherwise
   */
  public send(data: string | ArrayBuffer, type: MessageType = MessageType.TEXT): boolean {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('Cannot send message: WebSocket is not connected');
      return false;
    }

    try {
      // Send the data based on type
      if (type === MessageType.BINARY && data instanceof ArrayBuffer) {
        this.ws.send(data);
      } else if (type === MessageType.TEXT && typeof data === 'string') {
        this.ws.send(data);
      } else {
        console.error('Invalid data type for message type');
        return false;
      }

      // Create message object for logging
      const message: Message = {
        id: this.generateMessageId(),
        type,
        data,
        timestamp: new Date(),
        direction: 'sent',
      };

      // Notify handler
      if (this.eventHandlers.onMessage) {
        this.eventHandlers.onMessage(message);
      }

      return true;
    } catch (error) {
      console.error('Failed to send message:', error);
      return false;
    }
  }

  /**
   * Gets the current connection status
   */
  public getStatus(): ConnectionStatus {
    return this.currentStatus;
  }

  /**
   * Checks if the WebSocket is currently connected
   */
  public isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }

  /**
   * Handles WebSocket open event
   */
  private handleOpen(): void {
    console.log('WebSocket connection established');
    this.updateStatus(ConnectionStatus.CONNECTED);
    this.reconnectAttempts = 0;
    this.lastPongReceived = Date.now();
    
    // Start health check mechanism
    this.startHealthCheck();
  }

  /**
   * Handles incoming WebSocket messages
   */
  private handleMessage(event: MessageEvent): void {
    // Determine message type based on data type
    const messageType = event.data instanceof ArrayBuffer 
      ? MessageType.BINARY 
      : MessageType.TEXT;

    // Check if this is a pong response (application-level)
    if (messageType === MessageType.TEXT && event.data === 'pong') {
      this.handlePongReceived();
      return;
    }

    // Create message object
    const message: Message = {
      id: this.generateMessageId(),
      type: messageType,
      data: event.data,
      timestamp: new Date(),
      direction: 'received',
    };

    console.log('Message received:', message);

    // Notify handler
    if (this.eventHandlers.onMessage) {
      this.eventHandlers.onMessage(message);
    }
  }

  /**
   * Handles WebSocket error events
   */
  private handleError(event: Event): void {
    console.error('WebSocket error:', event);
    
    const error = new Error('WebSocket connection error');
    
    // Update status based on current state
    if (this.currentStatus === ConnectionStatus.CONNECTING) {
      this.updateStatus(ConnectionStatus.NO_ROUTE_TO_HOST);
    } else {
      this.updateStatus(ConnectionStatus.ERROR);
    }

    // Notify error handler
    if (this.eventHandlers.onError) {
      this.eventHandlers.onError(error);
    }

    this.handleReconnection();
  }

  /**
   * Handles WebSocket close events
   */
  private handleClose(event: CloseEvent): void {
    console.log(`WebSocket connection closed: ${event.code} - ${event.reason}`);
    
    this.stopHealthCheck();
    this.ws = null;

    if (!this.isManualDisconnect) {
      this.updateStatus(ConnectionStatus.DISCONNECTED);
      this.handleReconnection();
    }
  }

  /**
   * Starts the ping/pong health check mechanism
   */
  private startHealthCheck(): void {
    this.stopHealthCheck();

    // Send ping at regular intervals
    this.pingIntervalId = window.setInterval(() => {
      this.sendPing();
    }, this.config.pingInterval);
  }

  /**
   * Stops the health check mechanism
   */
  private stopHealthCheck(): void {
    if (this.pingIntervalId !== null) {
      clearInterval(this.pingIntervalId);
      this.pingIntervalId = null;
    }

    if (this.pongTimeoutId !== null) {
      clearTimeout(this.pongTimeoutId);
      this.pongTimeoutId = null;
    }
  }

  /**
   * Sends a ping message to check connection health
   */
  private sendPing(): void {
    if (!this.isConnected()) {
      return;
    }

    try {
      // Send application-level ping (as text message)
      // Note: Browser WebSocket API doesn't expose control frames directly
      this.ws!.send('ping');
      
      console.log('Ping sent, waiting for pong...');

      // Set timeout for pong response
      this.pongTimeoutId = window.setTimeout(() => {
        console.error('Pong timeout - connection may be dead');
        this.updateStatus(ConnectionStatus.ERROR);
        this.disconnect(1000, 'Ping timeout');
      }, this.config.pongTimeout);
    } catch (error) {
      console.error('Failed to send ping:', error);
    }
  }

  /**
   * Handles pong response received from server
   */
  private handlePongReceived(): void {
    console.log('Pong received - connection is healthy');
    
    this.lastPongReceived = Date.now();

    // Clear pong timeout
    if (this.pongTimeoutId !== null) {
      clearTimeout(this.pongTimeoutId);
      this.pongTimeoutId = null;
    }
  }

  /**
   * Handles automatic reconnection logic
   */
  private handleReconnection(): void {
    if (!this.config.autoReconnect || this.isManualDisconnect) {
      return;
    }

    if (this.reconnectAttempts >= this.config.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached');
      this.updateStatus(ConnectionStatus.ERROR);
      return;
    }

    this.reconnectAttempts++;
    this.updateStatus(ConnectionStatus.RECONNECTING);

    console.log(
      `Attempting to reconnect (${this.reconnectAttempts}/${this.config.maxReconnectAttempts})...`
    );

    this.reconnectTimeoutId = window.setTimeout(() => {
      this.connect();
    }, this.config.reconnectDelay);
  }

  /**
   * Clears the reconnection timeout
   */
  private clearReconnectTimeout(): void {
    if (this.reconnectTimeoutId !== null) {
      clearTimeout(this.reconnectTimeoutId);
      this.reconnectTimeoutId = null;
    }
  }

  /**
   * Updates the connection status and notifies handlers
   */
  private updateStatus(status: ConnectionStatus): void {
    if (this.currentStatus === status) {
      return;
    }

    console.log(`Status changed: ${this.currentStatus} -> ${status}`);
    this.currentStatus = status;

    if (this.eventHandlers.onStatusChange) {
      this.eventHandlers.onStatusChange(status);
    }
  }

  /**
   * Generates a unique message ID
   */
  private generateMessageId(): string {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Gets time since last pong was received (in milliseconds)
   */
  public getTimeSinceLastPong(): number {
    return Date.now() - this.lastPongReceived;
  }
}
