package org.messegeserver

import org.kodein.di.direct
import org.kodein.di.instance
import org.messegeserver.util.logger

//TODO move to properties
object Application {
    private val logger = logger()
    
    var WS_PATH: String = "/api/v1/chat/ws"
    var WS_PORT: Int = 7777

    @JvmStatic
    fun main(args: Array<String>) {
        logger.info("=".repeat(60))
        logger.info("Starting Messenger WebSocket Server")
        logger.info("=".repeat(60))
        logger.info("Configuration:")
        logger.info("  - WebSocket Path: {}", WS_PATH)
        logger.info("  - WebSocket Port: {}", WS_PORT)
        logger.info("=".repeat(60))
        
        try {
            val diContainer = applicationContainer(WS_PATH, WS_PORT)
            logger.debug("Dependency injection container initialized successfully")

            val nettyServer = diContainer.direct.instance<NettyServer>()
            logger.debug("NettyServer instance created")

            logger.info("Starting Netty server...")
            nettyServer.start()
            
            logger.info("Server started successfully")
        } catch (e: Exception) {
            logger.error("Failed to start server", e)
            throw e
        }
    }
}
