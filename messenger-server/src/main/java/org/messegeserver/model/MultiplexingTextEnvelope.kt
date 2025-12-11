package org.messegeserver.model

data class MultiplexingTextEnvelope(
    val chanel_id: String? = null,
    val message_id: String? = null,
    val payload: Any? = null,
    val type: String? = null,
)
