package wsclient

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import org.example.wsclient.WebSocketChanelInitializer
import java.net.URI


class WebSocketClient(
    uri: URI,
    webSocketChanelInitializer: WebSocketChanelInitializer,
) {
    private val host = uri.host ?: "127.0.0.1"
    private val port = uri.port

    private val group: EventLoopGroup = NioEventLoopGroup()
    private val channel: Channel

    init {
        try {
            channel = Bootstrap().group(group)
                .channel(NioSocketChannel::class.java)
                .handler(webSocketChanelInitializer)
                .connect(host, port)
                .await()
                .channel()

        } catch (e: Exception) {
            group.shutdownGracefully()
            throw e
        }
    }

    fun pushMessage(frame: WebSocketFrame): ChannelFuture =
        channel.writeAndFlush(frame)

    fun close(): ChannelFuture =
        try {
            channel.writeAndFlush(CloseWebSocketFrame())
            channel.closeFuture().await()
        } finally {
            group.shutdownGracefully()
        }

}