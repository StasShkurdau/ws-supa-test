package org.example.wsclient.handler

import io.netty.channel.*
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.websocketx.*


class WebSocketHandshakeClientHandler(
    private val wsHandshaker: WebSocketClientHandshaker,
    private val webSocketClientHandler: WebSocketClientHandler
) : SimpleChannelInboundHandler<FullHttpResponse>() {
    private var handshakeFuture: ChannelPromise? = null

    fun handshakeFuture(): ChannelFuture? {
        return handshakeFuture
    }

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        handshakeFuture = ctx.newPromise()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        wsHandshaker.handshake(ctx.channel())
    }

    @Throws(Exception::class)
    public override fun channelRead0(ctx: ChannelHandlerContext, message: FullHttpResponse) {
        val chanel = ctx.channel()

        if (!wsHandshaker.isHandshakeComplete) {
            try {
                wsHandshaker.finishHandshake(chanel, message)
                handshakeFuture!!.setSuccess()

                //Replace handshake handler to ws frame handler
                chanel.pipeline().remove(this)
                chanel.pipeline().addLast(webSocketClientHandler)
            } catch (e: WebSocketHandshakeException) {
                handshakeFuture!!.setFailure(e)
            }
        }
    }
}