package org.messegeserver.wsmextension.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelId
import java.util.concurrent.ConcurrentHashMap

class MultipleFrameMessageHandler {
    private val processedMessagesCache = ConcurrentHashMap<String, Array<ByteArray?>>()

    fun handle(
        chanelContext: ChannelHandlerContext,
        messageId: Long,
        frameMessage: ByteBuf,
        payloadHandler: (frameMessage: ByteArray) -> Unit
    ) {
        val contextMessageId = getContextMessageId(chanelContext.channel().id(), messageId)

        val frameNumber = frameMessage.readInt()
        val totalFramesInMessage = frameMessage.readInt()
        val frameData = frameMessage.array()

        synchronized(contextMessageId) {
            val messageIdFramesPayloadList = if (processedMessagesCache.contains(contextMessageId)) {
                processedMessagesCache.getValue(contextMessageId)
            } else {
                //TODO to concurrent list
                val newFramePayloadList = Array<ByteArray?>(totalFramesInMessage) { null }

                processedMessagesCache[contextMessageId] = newFramePayloadList

                newFramePayloadList
            }

            messageIdFramesPayloadList[frameNumber] = frameData

            if (messageIdFramesPayloadList.allElementsFilled()) {
                val message = messageIdFramesPayloadList.filterNotNull().reduce { acc, byteArray ->
                    acc + byteArray
                }
                //TODO remove from synchronized
                payloadHandler(message)
            }
        }

    }

    private fun getContextMessageId(chanelId: ChannelId, messageId: Long) =
        chanelId.asLongText() + messageId

    private fun Array<ByteArray?>.allElementsFilled() = none { it == null }

}