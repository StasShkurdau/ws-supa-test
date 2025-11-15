package org.messegeserver.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import org.messegeserver.util.logger

class BinaryWebSocketHandler : SimpleChannelInboundHandler<BinaryWebSocketFrame>() {
    private val logger = logger()

    override fun channelRead0(chanelContext: ChannelHandlerContext?, webSocketFrame: BinaryWebSocketFrame?) {
        if (chanelContext == null || webSocketFrame == null) {
            logger.warn("Received null context or frame")
            return
        }

        val dataLength = webSocketFrame.content().readableBytes()
        logger.debug("Received binary WebSocket frame: {} bytes from channel {}", 
                     dataLength, chanelContext.channel().id().asShortText())
        
        //TODO implement read message
        logger.trace("Binary frame content processing not yet implemented")
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        if (ctx == null) return
        
        //TODO remove deprecated HANDSHAKE_COMPLETE
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            logger.info("WebSocket handshake completed for channel {}", ctx.channel().id().asShortText())
            logger.debug("Removing HandshakeRequestHandler from pipeline")
            
            ctx.pipeline().remove(HandshakeRequestHandler::class.java)
            
            logger.debug("Client {} successfully connected via WebSocket", 
                        ctx.channel().remoteAddress())
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