package org.messegeserver.wsmextension.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelId
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Message processing occurs through payloadHandle function
 * Provides EXACTLY-ONCE message processing using processedMessageIdsByPlayerIds
 */
class SingleFrameMessageHandler {

    //TODO create cache, clean all messageIds after 5 minutes
    //Better inject like cache
    //Cache contains processing and already processed messages
    private val processedMessagesCache = CopyOnWriteArraySet<String>()

    fun handle(
        chanelContext: ChannelHandlerContext,
        messageId: Long,
        frameMessage: ByteBuf,
        payloadHandler: (frameMessage: ByteBuf) -> Unit
    ) {
        val fullMessageId = getContextMessageId(chanelContext.channel().id(), messageId)

        if (isMessageAlreadyProcessed(fullMessageId)) {
            return //TODO add frame with type "MESSAGE_ALREADY_PROCESSED" and send in this case
        } else {
            //Add fullMessageId as processing
            processedMessagesCache.add(fullMessageId)
        }

        try {
            payloadHandler(frameMessage)
        } catch (e: Exception) {
            //Clean cache on handling error
            processedMessagesCache.remove(fullMessageId)
            //TODO add log
        }
    }

    private fun getContextMessageId(chanelId: ChannelId, messageId: Long) =
        chanelId.asLongText() + messageId

    private fun isMessageAlreadyProcessed(fullMessageId: String): Boolean =
        processedMessagesCache.contains(fullMessageId)

}