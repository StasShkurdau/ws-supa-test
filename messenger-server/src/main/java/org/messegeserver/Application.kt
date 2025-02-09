package org.messegeserver

import org.kodein.di.direct
import org.kodein.di.instance

//TODO move to properties
object Application {
    var WS_PATH: String = ""
    var WS_PORT: Int = 7777

    @JvmStatic
    fun main(args: Array<String>) {
        val diContainer = applicationContainer(WS_PATH, WS_PORT)

        val nettyServer = diContainer.direct.instance<NettyServer>()

        nettyServer.start()
    }
}
