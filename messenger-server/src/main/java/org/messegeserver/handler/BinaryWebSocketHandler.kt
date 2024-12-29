package org.messegeserver.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler

class  BinaryWebSocketHandler() : SimpleChannelInboundHandler<BinaryWebSocketFrame>() {
    override fun channelRead0(chanelContext: ChannelHandlerContext?, webSocketFrame: BinaryWebSocketFrame?) {
        //TODO implement read message
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        //TODO remove deprecated HANDSHAKE_COMPLETE
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx?.pipeline()?.remove(HandshakeRequestHandler::class.java)
            //TODO Add log
        }

    }
}