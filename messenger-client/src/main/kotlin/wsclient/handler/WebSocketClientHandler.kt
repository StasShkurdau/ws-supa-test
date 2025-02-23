package org.example.wsclient.handler

import io.netty.channel.*
import io.netty.handler.codec.http.websocketx.*


class WebSocketClientHandler : SimpleChannelInboundHandler<WebSocketFrame>() {

    @Throws(Exception::class)
    public override fun channelRead0(ctx: ChannelHandlerContext, frame: WebSocketFrame) {
        val channel = ctx.channel()

        when (frame) {
            is TextWebSocketFrame -> {
                TODO()
            }

            is BinaryWebSocketFrame -> {
                TODO()
            }

            is PongWebSocketFrame -> {
                TODO()
            }

            is CloseWebSocketFrame -> {
                channel.close()
            }
        }
    }
}