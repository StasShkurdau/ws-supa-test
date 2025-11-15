package org.messegeserver.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Logger factory utility for creating SLF4J loggers
 * 
 * Usage:
 * ```kotlin
 * class MyClass {
 *     private val logger = logger()
 *     
 *     fun myMethod() {
 *         logger.info("This is an info message")
 *         logger.debug("Debug information: {}", someVariable)
 *         logger.error("Error occurred", exception)
 *     }
 * }
 * ```
 */

/**
 * Extension function to get logger for any class
 * Automatically uses the class name as logger name
 */
inline fun <reified T : Any> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

/**
 * Get logger by explicit name
 */
fun logger(name: String): Logger {
    return LoggerFactory.getLogger(name)
}

/**
 * Get logger for a specific class
 */
fun logger(clazz: Class<*>): Logger {
    return LoggerFactory.getLogger(clazz)
}
