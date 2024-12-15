package org.messegeserver.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest

class HandshakeRequestHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun channelRead0(p0: ChannelHandlerContext?, p1: FullHttpRequest?) {
        TODO("Not yet implemented")
    }
}