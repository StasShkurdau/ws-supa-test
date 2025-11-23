package org.messegeserver.handler

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import org.messegeserver.util.logger
import org.messegeserver.wsmextension.WsmExtensionFrameTypes
import org.messegeserver.wsmextension.WsmExtensionFrameTypes.MULTIPLE_FRAME_MESSAGE
import org.messegeserver.wsmextension.WsmExtensionFrameTypes.SINGLE_FRAME_MESSAGE
import org.messegeserver.wsmextension.handler.MultipleFrameMessageHandler
import org.messegeserver.wsmextension.handler.SingleFrameMessageHandler

/**
 * Protocol extension headers:
 * from 1 to 8 bytes - messageId
 * from 9 to 10 bytes - frame type
 * from 9 to 12 - frame number
 * from 13 to 16 - total number of frames
 */
class MultiplexingWebSocketHandlerWithMultiplexing(
    private val singleFrameMessageHandler: SingleFrameMessageHandler,
    private val multipleFrameMessageHandler: MultipleFrameMessageHandler,
) : SimpleChannelInboundHandler<WebSocketFrame>() {
    private val logger = logger()

    //TODO remove
    @OptIn(ExperimentalStdlibApi::class)
    private val userHandlerCode: (frameMessage: ByteArray) -> Unit = { frameMessage ->
        println(frameMessage.toHexString())
    }


    override fun channelRead0(ch: ChannelHandlerContext?, webSocketFrame: WebSocketFrame?) {
        val chanelContext = requireNotNull(ch)
        requireNotNull(webSocketFrame)

        when (webSocketFrame) {
            is TextWebSocketFrame -> {
                handleTextFrame(chanelContext, webSocketFrame)
            }
            is BinaryWebSocketFrame -> {
                handleBinaryFrame(chanelContext, webSocketFrame)
            }
            is PingWebSocketFrame -> {
                logger.debug("Received PING frame from channel {}", chanelContext.channel().id().asShortText())
            }
            is PongWebSocketFrame -> {
                logger.debug("Received PONG frame from channel {}", chanelContext.channel().id().asShortText())
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
     * Handles binary WebSocket frames (multiplexing protocol)
     */
    private fun handleBinaryFrame(ctx: ChannelHandlerContext, webSocketFrame: BinaryWebSocketFrame) {
        val frameMessage = webSocketFrame.content()

        val messageId = frameMessage.readLong()
        val frameType = WsmExtensionFrameTypes.valueOf(frameMessage.readShort())

        when (frameType) {
            SINGLE_FRAME_MESSAGE -> processSingleFrameMessage(
                chanelContext = ctx,
                messageId = messageId,
                frameMessage = frameMessage
            )

            MULTIPLE_FRAME_MESSAGE -> processMultipleFrameMessage(
                chanelContext = ctx,
                messageId = messageId,
                frameMessage = frameMessage
            )
        }

        //TODO implement read message
    }

    private fun processSingleFrameMessage(
        chanelContext: ChannelHandlerContext,
        messageId: Long,
        frameMessage: ByteBuf,
    ) = singleFrameMessageHandler.handle(chanelContext, messageId, frameMessage, userHandlerCode)


    private fun processMultipleFrameMessage(
        chanelContext: ChannelHandlerContext,
        messageId: Long,
        frameMessage: ByteBuf
    ) = multipleFrameMessageHandler.handle(chanelContext, messageId, frameMessage, userHandlerCode)

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        if (ctx == null) return
        
        when (evt) {
            //TODO remove deprecated HANDSHAKE_COMPLETE
            WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE -> {
                logger.info("WebSocket handshake completed (with multiplexing) for channel {}", 
                           ctx.channel().id().asShortText())
                ctx.pipeline().remove(HandshakeRequestHandler::class.java)
                logger.debug("Client {} successfully connected via WebSocket with multiplexing extension", 
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
}