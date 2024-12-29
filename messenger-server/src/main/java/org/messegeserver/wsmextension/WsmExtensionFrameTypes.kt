package org.messegeserver.wsmextension

enum class WsmExtensionFrameTypes(frameCode: Int) {
    SINGLE_FRAME_MESSAGE(1),
    MULTIPLE_FRAME_MESSAGE(2);

    companion object {
        fun valueOf(frameCode: Short) = when (frameCode.toInt()) {
            1 -> SINGLE_FRAME_MESSAGE
            2 -> MULTIPLE_FRAME_MESSAGE
            else -> error("Unknown frame code $frameCode")
        }
    }

}