package kr.elvis.playgroundspringdataredispubsub

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger { }

class Receiver {
    private val counter = AtomicInteger()

    fun receiveMessage(message: String) {
        logger.info { "Received < $message >" }
        counter.incrementAndGet()
    }

    fun getCount() = counter.get()
}
