package org.messegeserver.handler

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import org.messegeserver.util.logger

@Sharable
class BaseWebSocketHandler : SimpleChannelInboundHandler<WebSocketFrame>() {
    private val logger = logger()

    override fun channelRead0(chanelContext: ChannelHandlerContext?, webSocketFrame: WebSocketFrame?) {
        if (chanelContext == null || webSocketFrame == null) {
            logger.warn("Received null context or frame")
            return
        }

        when (webSocketFrame) {
            is TextWebSocketFrame -> {
                handleTextFrame(chanelContext, webSocketFrame)
            }
            is BinaryWebSocketFrame -> {
                handleBinaryFrame(chanelContext, webSocketFrame)
            }
            else -> {
                logger.warn("Received unsupported WebSocket frame type: {}", webSocketFrame.javaClass.simpleName)
            }
        }
    }
    
    /**
     * Handles text WebSocket frames (application-level ping/pong)
     */
    private fun handleTextFrame(ctx: ChannelHandlerContext, frame: TextWebSocketFrame) {
        val text = frame.text()
        logger.debug("Received text frame: '{}' from channel {}", text, ctx.channel().id().asShortText())
        
        // Handle application-level ping/pong
        when (text) {
            "ping" -> {
                logger.debug("Received application-level PING, responding with PONG")
                ctx.writeAndFlush(TextWebSocketFrame("pong"))
            }
            "pong" -> {
                logger.debug("Received application-level PONG")
            }
            else -> {
                logger.debug("Received text message: {}", text)
                // TODO: Handle other text messages
            }
        }
    }
    
    /**
     * Handles binary WebSocket frames (business logic)
     */
    private fun handleBinaryFrame(ctx: ChannelHandlerContext, frame: BinaryWebSocketFrame) {
        logger.debug("Received binary WebSocket frame: {} bytes from channel {}",
            frame.content().readableBytes(), ctx.channel().id().asShortText())
        
        val content = frame.content()

        val bytes = ByteArray(content.readableBytes())
        content.getBytes(content.readerIndex(), bytes)

        logger.info("Received binary bytes: {}", bytes.joinToString(","))
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        if (ctx == null) return
        
        when (evt) {
            //TODO remove deprecated HANDSHAKE_COMPLETE
            WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE -> {
                logger.info("WebSocket handshake completed for channel {}", ctx.channel().id().asShortText())
                logger.debug("Removing HandshakeRequestHandler from pipeline")
                
                ctx.pipeline().remove(HandshakeRequestHandler::class.java)
                
                logger.debug("Client {} successfully connected via WebSocket", 
                            ctx.channel().remoteAddress())
            }
            
            is IdleStateEvent -> {
                handleIdleStateEvent(ctx, evt)
            }
            
            else -> super.userEventTriggered(ctx, evt)
        }
    }
    
    /**
     * Handles idle state events for connection health monitoring
     * Sends PING on reader idle, closes connection if no response
     */
    private fun handleIdleStateEvent(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
        when (evt.state()) {
            IdleState.READER_IDLE -> {
                logger.warn("No data received from channel {} for {} seconds, sending PING", 
                           ctx.channel().id().asShortText(), 
                           evt.state())
                
                // Send PING frame to check if connection is alive
                // WebSocketServerProtocolHandler will automatically handle the PONG response
                val pingFrame = PingWebSocketFrame(Unpooled.wrappedBuffer("heartbeat".toByteArray()))
                ctx.writeAndFlush(pingFrame).addListener { future ->
                    if (!future.isSuccess) {
                        logger.error("Failed to send PING frame to channel {}, closing connection", 
                                   ctx.channel().id().asShortText(), 
                                   future.cause())
                        ctx.close()
                    } else {
                        logger.debug("PING frame sent to channel {}", ctx.channel().id().asShortText())
                    }
                }
            }
            
            IdleState.WRITER_IDLE -> {
                logger.debug("Writer idle for channel {}", ctx.channel().id().asShortText())
            }
            
            IdleState.ALL_IDLE -> {
                logger.warn("Connection idle (both read and write) for channel {}, closing", 
                           ctx.channel().id().asShortText())
                ctx.close()
            }
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        if (ctx != null) {
            logger.info("Channel active: {} from {}", 
                       ctx.channel().id().asShortText(), 
                       ctx.channel().remoteAddress())
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        if (ctx != null) {
            logger.info("Channel inactive: {} from {}", 
                       ctx.channel().id().asShortText(), 
                       ctx.channel().remoteAddress())
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        if (ctx != null && cause != null) {
            logger.error("Exception in WebSocket handler for channel {}", 
                        ctx.channel().id().asShortText(), cause)
            ctx.close()
        }
    }
}