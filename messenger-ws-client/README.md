# WebSocket Client

A modern WebSocket client application built with React, TypeScript, and Vite.

## Features

- **WebSocket Connection Management**: Connect to any WebSocket server with custom host and port
- **Connection Health Monitoring**: Automatic ping/pong health checks to monitor connection status
- **Multiple Message Types**: Support for TEXT, BINARY, and other WebSocket frame types
- **Real-time Status Display**: Visual feedback for connection states (CONNECTED, NOT_STARTED, NO_ROUTE_TO_HOST, etc.)
- **Modern UI**: Built with React and TailwindCSS for a responsive, user-friendly interface
- **Auto-Reconnection**: Automatic reconnection with configurable retry attempts
- **Message History**: Complete log of sent and received messages with timestamps

## Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn/pnpm

### Installation

```bash
npm install
```

### Development

Start the development server on port 3001:

```bash
npm run dev
```

Open your browser and navigate to `http://localhost:3001`

### Build

Build for production:

```bash
npm run build
```

Preview production build:

```bash
npm run preview
```

## Testing with Test Server

A simple WebSocket test server is included for testing purposes.

### Start Test Server

```bash
# Install dependencies (first time only)
npm install --prefix . --package-lock-only ws

# Start server on default port 8080
node test-server.js

# Or specify custom port
node test-server.js 9000
```

The test server will:
- Echo back all text messages with "Echo: " prefix
- Respond to "ping" messages with "pong"
- Echo back binary data
- Log all activity to console

### Connect Client to Test Server

1. Start the test server (see above)
2. Open the web client at `http://localhost:3001`
3. Enter `localhost` as server address
4. Enter `8080` as port (or your custom port)
5. Click "Connect"
6. Send messages and observe the echo responses

## Usage

### Connecting to a Server

1. Enter the WebSocket server address (e.g., `localhost` or `192.168.1.100`)
2. Enter the port number (e.g., `8080`)
3. Click "Connect" to establish a WebSocket connection
4. Monitor the connection status indicator

### Sending Messages

#### Text Messages
1. Select "TEXT" message type
2. Enter your message in the text area
3. Click "Send"

#### Binary Messages
1. Select "BINARY" message type
2. Either:
   - Enter text (will be converted to binary)
   - Click "Send File" to upload a file as binary data

### Connection Status Indicators

- **NOT_STARTED**: Connection has not been initiated
- **CONNECTING**: Attempting to establish connection
- **CONNECTED**: Successfully connected (green indicator)
- **DISCONNECTING**: Connection is being closed
- **DISCONNECTED**: Connection closed normally
- **NO_ROUTE_TO_HOST**: Cannot reach server (check address/network)
- **ERROR**: Connection error occurred
- **RECONNECTING**: Attempting automatic reconnection

### Health Monitoring

The client automatically sends ping messages every 30 seconds to check connection health:
- If no pong response is received within 5 seconds, connection is considered dead
- Client will attempt to reconnect automatically (up to 5 attempts)
- Health check activity is logged in browser console

## Architecture

### Core Components

- **WebSocketClient** (`src/services/WebSocketClient.ts`): Core WebSocket client with automatic reconnection and health checks
- **useWebSocket** (`src/hooks/useWebSocket.ts`): React hook for WebSocket state management
- **ConnectionForm** (`src/components/ConnectionForm.tsx`): UI for connection configuration
- **StatusIndicator** (`src/components/StatusIndicator.tsx`): Visual connection status display
- **MessageInput** (`src/components/MessageInput.tsx`): Message composition and sending
- **MessageList** (`src/components/MessageList.tsx`): Message history display

### Type Definitions

- **ConnectionStatus**: Enum defining all possible connection states
- **MessageType**: Support for different WebSocket frame types (TEXT, BINARY, PING, PONG, CLOSE)
- **Message**: Interface for message objects with metadata
- **WebSocketConfig**: Configuration options for WebSocket connection

### Configuration Options

```typescript
{
  host: string;              // Server hostname or IP
  port: number;              // Server port
  path?: string;             // WebSocket endpoint path (default: '/')
  secure?: boolean;          // Use wss:// instead of ws://
  pingInterval?: number;     // Ping interval in ms (default: 30000)
  pongTimeout?: number;      // Pong timeout in ms (default: 5000)
  autoReconnect?: boolean;   // Enable auto-reconnect (default: true)
  maxReconnectAttempts?: number;  // Max reconnect attempts (default: 5)
  reconnectDelay?: number;   // Delay between reconnects in ms (default: 3000)
}
```

## Best Practices Implemented

- **TypeScript**: Full type safety throughout the application
- **Automatic Health Checks**: Ping/pong mechanism to detect dead connections
- **Proper Lifecycle Management**: Clean connection setup and teardown
- **Error Handling**: Comprehensive error handling and recovery
- **Separation of Concerns**: Clear separation between business logic and UI
- **Comprehensive Documentation**: Detailed comments in English throughout codebase
- **Modern React Patterns**: Hooks, functional components, proper state management
- **Responsive Design**: Mobile-friendly UI with TailwindCSS
- **Accessibility**: Semantic HTML and proper ARIA labels

## Project Structure

```
messenger-ws-client/
├── src/
│   ├── components/          # React UI components
│   │   ├── ConnectionForm.tsx
│   │   ├── StatusIndicator.tsx
│   │   ├── MessageInput.tsx
│   │   └── MessageList.tsx
│   ├── hooks/              # Custom React hooks
│   │   └── useWebSocket.ts
│   ├── services/           # Business logic
│   │   └── WebSocketClient.ts
│   ├── types/              # TypeScript type definitions
│   │   └── index.ts
│   ├── App.tsx             # Main application component
│   ├── main.tsx            # Application entry point
│   └── index.css           # Global styles
├── public/                 # Static assets
├── test-server.js          # Test WebSocket server
├── index.html              # HTML template
├── package.json            # Dependencies and scripts
├── tsconfig.json           # TypeScript configuration
├── vite.config.ts          # Vite configuration
└── tailwind.config.js      # TailwindCSS configuration
```

## Troubleshooting

### Cannot connect to server
- Verify server is running and accessible
- Check firewall settings
- Ensure correct host and port
- Try using IP address instead of hostname

### Connection drops frequently
- Check network stability
- Verify server is responding to ping messages
- Increase ping interval in configuration
- Check server logs for errors

### Messages not appearing
- Verify connection status is "CONNECTED"
- Check browser console for errors
- Ensure server is echoing messages back

## License

MIT
