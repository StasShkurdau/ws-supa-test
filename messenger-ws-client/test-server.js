/**
 * Simple WebSocket test server for demonstration purposes
 * Responds to ping messages with pong and echoes back received messages
 * 
 * Usage: node test-server.js [port]
 * Default port: 8080
 */

const WebSocket = require('ws');

// Get port from command line argument or use default
const PORT = process.argv[2] || 8080;

// Create WebSocket server
const wss = new WebSocket.Server({ port: PORT });

console.log(`WebSocket test server started on port ${PORT}`);
console.log(`Clients can connect to: ws://localhost:${PORT}`);

// Handle new connections
wss.on('connection', (ws, req) => {
  const clientIp = req.socket.remoteAddress;
  console.log(`\n[${new Date().toISOString()}] New client connected from ${clientIp}`);
  
  // Send welcome message
  ws.send('Welcome to WebSocket test server!');

  // Handle incoming messages
  ws.on('message', (data, isBinary) => {
    const messageType = isBinary ? 'BINARY' : 'TEXT';
    
    if (!isBinary) {
      const message = data.toString();
      console.log(`[${new Date().toISOString()}] Received ${messageType}: ${message}`);
      
      // Handle ping/pong
      if (message === 'ping') {
        console.log(`[${new Date().toISOString()}] Responding with pong`);
        ws.send('pong');
        return;
      }
      
      // Echo back the message
      const response = `Echo: ${message}`;
      console.log(`[${new Date().toISOString()}] Sending: ${response}`);
      ws.send(response);
    } else {
      // Handle binary data
      console.log(`[${new Date().toISOString()}] Received ${messageType}: ${data.length} bytes`);
      
      // Echo back binary data
      console.log(`[${new Date().toISOString()}] Echoing back ${data.length} bytes`);
      ws.send(data, { binary: true });
    }
  });

  // Handle connection close
  ws.on('close', (code, reason) => {
    console.log(`[${new Date().toISOString()}] Client disconnected: ${code} - ${reason}`);
  });

  // Handle errors
  ws.on('error', (error) => {
    console.error(`[${new Date().toISOString()}] WebSocket error:`, error.message);
  });

  // Handle pong frames (response to ping frames)
  ws.on('pong', () => {
    console.log(`[${new Date().toISOString()}] Received pong frame`);
  });
});

// Handle server errors
wss.on('error', (error) => {
  console.error('Server error:', error);
});

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('\nShutting down server...');
  wss.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});

console.log('\nServer is ready to accept connections.');
console.log('Press Ctrl+C to stop the server.\n');
