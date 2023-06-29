package kr.elvis.playgroundspringdataredispubsub

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import java.util.regex.Pattern
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

@SpringBootApplication
class PlaygroundSpringDataRedisPubsubApplication {
    @Bean
    fun container(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter,
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        return container
    }

    @Bean
    fun listenerAdapter(receiver: Receiver) = MessageListenerAdapter(receiver, "receiveMessage")

    @Bean
    fun receiver() = Receiver()

    @Bean
    fun template(connectionFactory: RedisConnectionFactory) = StringRedisTemplate(connectionFactory)
}

fun main(args: Array<String>) {
    val ctx = runApplication<PlaygroundSpringDataRedisPubsubApplication>(*args)

    val container = ctx.getBean(RedisMessageListenerContainer::class.java)
    val listenerAdapter = ctx.getBean(MessageListenerAdapter::class.java)

    // adding message listener before container's listener initialization. (subscribe redis)
    container.addMessageListener(listenerAdapter, ChannelTopic("first chat"))

    // adding message listener after container's listener initialization. (subscribe redis)
    container.addMessageListener(listenerAdapter, ChannelTopic("second chat"))

    val template = ctx.getBean(StringRedisTemplate::class.java)
    val receiver = ctx.getBean(Receiver::class.java)

    while (receiver.getCount() == 0) {
        logger.info { "Sending message..." }
        // publish message to redis
        template.convertAndSend("first chat", "Hello from Redis!")
        Thread.sleep(500L)
    }

    exitProcess(0)
}
