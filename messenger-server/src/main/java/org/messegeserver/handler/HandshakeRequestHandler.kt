package org.messegeserver.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.timeout.IdleStateHandler
import io.netty.channel.ChannelHandler.Sharable
import org.messegeserver.util.logger
import java.util.concurrent.TimeUnit

@Sharable
class HandshakeRequestHandler(
    private val wsPath: String,
    private val webSocketHandler: BaseWebSocketHandler,
    private val webSocketHandlerWithMultiplexing: MultiplexingWebSocketHandlerWithMultiplexing,
) : SimpleChannelInboundHandler<FullHttpRequest>() {
    private val logger = logger()

    companion object {
        private const val WS_PROTOCOL_HEADER = "Sec-WebSocket-Protocol"
        private const val WS_MULTIPLEXING_PROTOCOL = "multiplexing"
        
        // PING/PONG configuration (best practices for WebSocket)
        private const val READER_IDLE_TIME_SECONDS = 60L  // Close if no data received for 60s
        private const val WRITER_IDLE_TIME_SECONDS = 0L   // Disabled
        private const val ALL_IDLE_TIME_SECONDS = 0L      // Disabled
        private const val HANDSHAKE_TIMEOUT_MILLIS = 10000L // 10 seconds for handshake
    }

    override fun channelRead0(chanelContext: ChannelHandlerContext?, httpRequest: FullHttpRequest?) {
        if (chanelContext == null) throw NullPointerException("chanelContext can't be null")
        if (httpRequest == null) throw NullPointerException("httpRequest can't be null")

        val containsWsPath = httpRequest.containsWsPath()

        logger.info("containsWsPath: $containsWsPath, uri(): ${httpRequest.uri()}")

        when {
            (containsWsPath && httpRequest.hasMultiplexingEnabled()) -> startWsWithMultiplexingExtension(
                chanelContext,
                httpRequest
            )

            (containsWsPath) -> startWsWithoutExtension(chanelContext, httpRequest)

            //TODO upgrade logic and dont use is100ContinueExpected
            (HttpHeaders.is100ContinueExpected(httpRequest)) -> sendContinueWithoutExtension(chanelContext)
        }
    }

    private fun sendContinueWithoutExtension(ctx: ChannelHandlerContext) {
        val response: FullHttpResponse = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE
        )
        ctx.writeAndFlush(response)
    }

    private fun FullHttpRequest.containsWsPath(): Boolean = uri().endsWith(wsPath)

    /**
     * Checks if multiplexing is enabled via Sec-WebSocket-Protocol header
     * The client sends: Sec-WebSocket-Protocol: multiplexing
     */
    private fun FullHttpRequest.hasMultiplexingEnabled(): Boolean {
        if (headers().contains(WS_PROTOCOL_HEADER)) {
            val protocols = headers().getAll(WS_PROTOCOL_HEADER)
            if (protocols.contains(WS_MULTIPLEXING_PROTOCOL)) {
                logger.debug("Multiplexing enabled via Sec-WebSocket-Protocol header")
                return true
            }
        }
        
        logger.debug("Multiplexing not enabled")
        return false
    }


    private fun startWsWithoutExtension(chanelContext: ChannelHandlerContext, httpRequest: FullHttpRequest) {
        logger.debug("Starting WebSocket connection without multiplexing extension")
        
        // Add IdleStateHandler to detect idle connections
        // If no read occurs for READER_IDLE_TIME_SECONDS, an IdleStateEvent will be triggered
        val idleStateHandler = IdleStateHandler(
            READER_IDLE_TIME_SECONDS,
            WRITER_IDLE_TIME_SECONDS,
            ALL_IDLE_TIME_SECONDS,
            TimeUnit.SECONDS
        )
        chanelContext.pipeline().addLast("idleStateHandler", idleStateHandler)
        
        // Configure WebSocketServerProtocolHandler with proper settings
        val wsConfig = WebSocketServerProtocolConfig.newBuilder()
            .websocketPath(wsPath)
            .checkStartsWith(false)  // Exact path match
            .handshakeTimeoutMillis(HANDSHAKE_TIMEOUT_MILLIS)
            .dropPongFrames(false)  // Let PONG frames reach handlers (for logging/debugging)
            .handleCloseFrames(true)  // Automatically handle CLOSE frames
            .sendCloseFrame(null)  // Send close frame on channel close
            .build()
        
        val wsProtocolHandler = WebSocketServerProtocolHandler(wsConfig)
        chanelContext.pipeline().addLast("wsProtocolHandler", wsProtocolHandler)
        chanelContext.pipeline().addLast("binaryWebSocketHandler", webSocketHandler)
        
        logger.debug("WebSocket pipeline configured: IdleStateHandler -> WebSocketServerProtocolHandler -> BinaryWebSocketHandler")
        
        chanelContext.fireChannelRead(httpRequest.retain()) // To next handler
    }

    private fun startWsWithMultiplexingExtension(chanelContext: ChannelHandlerContext, httpRequest: FullHttpRequest) {
        logger.debug("Starting WebSocket connection with multiplexing extension")
        
        // Add IdleStateHandler to detect idle connections
        val idleStateHandler = IdleStateHandler(
            READER_IDLE_TIME_SECONDS,
            WRITER_IDLE_TIME_SECONDS,
            ALL_IDLE_TIME_SECONDS,
            TimeUnit.SECONDS
        )
        chanelContext.pipeline().addLast("idleStateHandler", idleStateHandler)
        
        // Configure WebSocketServerProtocolHandler with multiplexing subprotocol support
        val wsConfig = WebSocketServerProtocolConfig.newBuilder()
            .websocketPath(wsPath)
            .subprotocols(WS_MULTIPLEXING_PROTOCOL)  // Accept "multiplexing" subprotocol
            .checkStartsWith(false)  // Exact path match
            .handshakeTimeoutMillis(HANDSHAKE_TIMEOUT_MILLIS)
            .dropPongFrames(false)  // Let PONG frames reach handlers (for logging/debugging)
            .handleCloseFrames(true)  // Automatically handle CLOSE frames
            .sendCloseFrame(null)  // Send close frame on channel close
            .build()
        
        val wsProtocolHandler = WebSocketServerProtocolHandler(wsConfig)
        chanelContext.pipeline().addLast("wsProtocolHandler", wsProtocolHandler)
        chanelContext.pipeline().addLast("binaryWebSocketHandlerWithMultiplexing", webSocketHandlerWithMultiplexing)
        
        logger.debug("WebSocket pipeline configured with multiplexing subprotocol: IdleStateHandler -> WebSocketServerProtocolHandler -> BinaryWebSocketHandlerWithMultiplexing")
        
        chanelContext.fireChannelRead(httpRequest.retain()) // To next handler
    }
}