package org.messegeserver

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.messegeserver.config.ChatServerInitializer
import org.messegeserver.util.logger

class NettyServer(
    private val chatServerInitializer: ChatServerInitializer,
    private val port: Int
) {
    private val logger = logger()

    fun start() {
        logger.info("Initializing Netty server on port {}", port)
        
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        
        logger.debug("Created boss and worker event loop groups")

        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(chatServerInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            logger.debug("Server bootstrap configured with SO_BACKLOG=128, SO_KEEPALIVE=true")
            logger.info("Binding to port {}...", port)
            
            val channel = bootstrap.bind(port).sync().channel()
            
            logger.info("Server successfully bound to port {}", port)
            logger.info("WebSocket server is ready to accept connections")
            
            // Wait until the server socket is closed
            channel.closeFuture().sync()
            
            logger.info("Server channel closed")
        } catch (e: Exception) {
            logger.error("Failed to start Netty server on port {}", port, e)
            throw e
        } finally {
            logger.info("Shutting down event loop groups...")
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
            logger.info("Server shutdown complete")
        }
    }
}