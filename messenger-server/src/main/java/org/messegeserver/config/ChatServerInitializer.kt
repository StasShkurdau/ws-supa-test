package org.messegeserver.config

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.stream.ChunkedWriteHandler
import org.messegeserver.handler.BinaryWebSocketHandler
import org.messegeserver.handler.HandshakeRequestHandler

class ChatServerInitializer: ChannelInitializer<Channel>() {

    companion object {
        private const val wsPath: String = "/api/v1/chat/ws"
    }

    override fun initChannel(chanel: Channel?) {
        val chanelPipeline: ChannelPipeline = requireNotNull(chanel).pipeline();

        chanelPipeline
            .addLast(HttpServerCodec())
            .addLast(ChunkedWriteHandler())
            .addLast(HttpObjectAggregator(64 * 1024))
            .addLast(HandshakeRequestHandler(wsPath))
            .addLast(WebSocketServerProtocolHandler(wsPath))
            .addLast(BinaryWebSocketHandler())
    }
}