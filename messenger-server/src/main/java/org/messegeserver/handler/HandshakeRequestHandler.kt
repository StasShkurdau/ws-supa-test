package org.messegeserver.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.channel.ChannelHandler.Sharable
import org.messegeserver.util.logger

@Sharable
class HandshakeRequestHandler(
    private val wsPath: String,
    private val webSocketHandler: BinaryWebSocketHandler,
    private val webSocketHandlerWithMultiplexing: BinaryWebSocketHandlerWithMultiplexing,
) : SimpleChannelInboundHandler<FullHttpRequest>() {
    private val logger = logger()

    companion object {
        private const val WS_EXTENSION_HEADER = "Sec-WebSocket-Extensions"
        private const val WS_MULTIPLEXING_EXTENSION = "Sus-Ws-multiplexing-Extension"
    }

    override fun channelRead0(chanelContext: ChannelHandlerContext?, httpRequest: FullHttpRequest?) {
        if (chanelContext == null) throw NullPointerException("chanelContext can't be null")
        if (httpRequest == null) throw NullPointerException("httpRequest can't be null")

        val containsWsPath = httpRequest.containsWsPath()

        logger.info("containsWsPath: $containsWsPath, uri(): ${httpRequest.uri()}")

        when {
            (containsWsPath && httpRequest.containsMultiplexingExtension()) -> startWsWithMultiplexingExtension(
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

    private fun FullHttpRequest.containsMultiplexingExtension(): Boolean =
        if (headers().contains(WS_EXTENSION_HEADER)) {
            val extensions = headers().getAll(WS_EXTENSION_HEADER)

            extensions.contains(WS_MULTIPLEXING_EXTENSION)
        } else {
            false
        }


    private fun startWsWithoutExtension(chanelContext: ChannelHandlerContext, httpRequest: FullHttpRequest) {
        chanelContext.pipeline().addLast(webSocketHandler)
        chanelContext.fireChannelRead(httpRequest.retain()) // To next handler
    }

    private fun startWsWithMultiplexingExtension(chanelContext: ChannelHandlerContext, httpRequest: FullHttpRequest) {
        chanelContext.pipeline().addLast(webSocketHandlerWithMultiplexing)
        chanelContext.fireChannelRead(httpRequest.retain()) // To next handler
    }
}