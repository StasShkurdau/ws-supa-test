package org.messegeserver

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.messegeserver.config.ChatServerInitializer
import org.messegeserver.handler.BaseWebSocketHandler
import org.messegeserver.handler.MultiplexingWebSocketHandlerWithMultiplexing
import org.messegeserver.handler.HandshakeRequestHandler
import org.messegeserver.wsmextension.handler.MultipleFrameMessageHandler
import org.messegeserver.wsmextension.handler.SingleFrameMessageHandler

fun applicationContainer(wsPath: String, port: Int) = DI {

    //Netty server
    bind<NettyServer>() with singleton { NettyServer(instance(), port) }
    bind<ChatServerInitializer>() with singleton { ChatServerInitializer(instance()) }

    // WS handler
    bind<BaseWebSocketHandler>() with singleton { BaseWebSocketHandler() }
    bind<MultiplexingWebSocketHandlerWithMultiplexing>() with singleton {
        MultiplexingWebSocketHandlerWithMultiplexing(
            instance(),
            instance()
        )
    }

    // Handshake
    bind<HandshakeRequestHandler>() with singleton { HandshakeRequestHandler(wsPath, instance(), instance()) }

    // Message handler
    bind<MultipleFrameMessageHandler>() with singleton { MultipleFrameMessageHandler() }
    bind<SingleFrameMessageHandler>() with singleton { SingleFrameMessageHandler() }
}
