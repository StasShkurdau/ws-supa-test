package org.messegeserver.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import org.messegeserver.wsmextension.WsmExtensionFrameTypes
import org.messegeserver.wsmextension.WsmExtensionFrameTypes.MULTIPLE_FRAME_MESSAGE
import org.messegeserver.wsmextension.WsmExtensionFrameTypes.SINGLE_FRAME_MESSAGE
import org.messegeserver.wsmextension.handler.MultipleFrameMessageHandler
import org.messegeserver.wsmextension.handler.SingleFrameMessageHandler

/**
 * Protocol extension headers:
 * from 1 to 8 bytes - messageId
 * from 9 to 10 bytes - frame type
 * from 9 to 12 - frame number
 * from 13 to 16 - total number of frames
 */
class BinaryWebSocketHandlerWithMultiplexing(
    private val singleFrameMessageHandler: SingleFrameMessageHandler,
    private val multipleFrameMessageHandler: MultipleFrameMessageHandler,
) : SimpleChannelInboundHandler<BinaryWebSocketFrame>() {

    //TODO remove
    @OptIn(ExperimentalStdlibApi::class)
    private val userHandlerCode: (frameMessage: ByteArray) -> Unit = { frameMessage ->
        println(frameMessage.toHexString())
    }


    override fun channelRead0(ch: ChannelHandlerContext?, webSocketFrame: BinaryWebSocketFrame?) {
        val chanelContext = requireNotNull(ch)

        val frameMessage = webSocketFrame!!.content()

        val messageId = frameMessage.readLong()

        val frameType = WsmExtensionFrameTypes.valueOf(frameMessage.readShort())

        when (frameType) {
            SINGLE_FRAME_MESSAGE -> processSingleFrameMessage(
                chanelContext = chanelContext,
                messageId = messageId,
                frameMessage = frameMessage
            )

            MULTIPLE_FRAME_MESSAGE -> processMultipleFrameMessage(
                chanelContext = chanelContext,
                messageId = messageId,
                frameMessage = frameMessage
            )
        }

        //TODO implement read message
    }

    private fun processSingleFrameMessage(
        chanelContext: ChannelHandlerContext,
        messageId: Long,
        frameMessage: ByteBuf,
    ) = singleFrameMessageHandler.handle(chanelContext, messageId, frameMessage, userHandlerCode)


    private fun processMultipleFrameMessage(
        chanelContext: ChannelHandlerContext,
        messageId: Long,
        frameMessage: ByteBuf
    ) = multipleFrameMessageHandler.handle(chanelContext, messageId, frameMessage, userHandlerCode)

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        //TODO remove deprecated HANDSHAKE_CO MPLETE
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx?.pipeline()?.remove(HandshakeRequestHandler::class.java)
            //TODO Add log
        }

    }
}