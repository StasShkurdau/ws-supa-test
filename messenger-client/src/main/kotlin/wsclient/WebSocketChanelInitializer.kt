package org.example.wsclient

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import org.example.wsclient.handler.WebSocketHandshakeClientHandler


class WebSocketChanelInitializer(
    private val webSocketHandshakeClientHandler: WebSocketHandshakeClientHandler,
) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(channel: SocketChannel) {
        channel
            .pipeline()
            .addLast(
                HttpClientCodec(),
                HttpObjectAggregator(8192),
                WebSocketClientCompressionHandler.INSTANCE,
                webSocketHandshakeClientHandler
            )
    }
}