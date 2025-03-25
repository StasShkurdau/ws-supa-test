package org.example

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import org.example.wsclient.WebSocketChanelInitializer
import org.kodein.di.direct
import org.kodein.di.instance
import wsclient.WebSocketClient
import java.net.URI
import java.nio.ByteBuffer
import java.util.Arrays

val URL = URI("ws://127.0.0.1:8080/websocket")

fun main() {

   val appContext = clientApplicationContainer(URL)

    val wsChanelInitializer = appContext.direct.instance<WebSocketChanelInitializer>()

    val client = WebSocketClient(URL, wsChanelInitializer)

    val message = ByteBuffer.allocate(42).put("firstMessage".toByteArray())

    val frame = BinaryWebSocketFrame()

    val content = frame.content()

    content.writeBytes(message)

    client.pushMessage(frame)
}