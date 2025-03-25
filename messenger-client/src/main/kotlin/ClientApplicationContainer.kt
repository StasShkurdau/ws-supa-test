package org.example

import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import org.example.wsclient.WebSocketChanelInitializer
import org.example.wsclient.handler.WebSocketClientHandler
import org.example.wsclient.handler.WebSocketHandshakeClientHandler
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import java.net.URI

fun clientApplicationContainer(uri: URI) = DI {

    //Netty server
    bind<WebSocketChanelInitializer>() with singleton { WebSocketChanelInitializer(instance()) }

    // WS handler
    bind<WebSocketClientHandler>() with singleton { WebSocketClientHandler() }

    // Handshake
    bind<WebSocketClientHandshaker>() with singleton {
        WebSocketClientHandshakerFactory.newHandshaker(
            uri, WebSocketVersion.V13, null, true, DefaultHttpHeaders()
        )
    }
    bind<WebSocketHandshakeClientHandler>() with singleton { WebSocketHandshakeClientHandler(instance(), instance()) }

}
