package com.iguigui.band

import com.orhanobut.logger.Logger
import com.rabbitmq.client.*
import java.util.*

private val mConnectionFactory by lazy {
    RMQProperties.let {
        ConnectionFactory().apply {
            host = it.getProperty("host")?.toString()
            port = it.getProperty("port")?.toString()?.toInt()!!
            username = it.getProperty("username").toString()
            password = it.getProperty("password")?.toString()
            connectionTimeout = 5000
            virtualHost = "dev"
        }

    }
}

private val mConnection by lazy {
    mConnectionFactory.newConnection()
}

val channel: Channel by lazy {
    val createChannel = mConnection.createChannel()
    createChannel.queueBind("clientInfo", "amq.direct", "clientInfo")
    createChannel
}

lateinit var RMQProperties : Properties


fun setUpConnectionFactory() {
    //建立连接
    val thread = Thread {
        Logger.d("thread start")
        // 创建连接
        val channel = mConnection.createChannel()
        //将队列绑定到消息交换机 exchange 上
        channel.queueBind("serverInfo", "amq.direct", "serverInfo")
        //创建消费者
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String?,
                envelope: Envelope?,
                properties: AMQP.BasicProperties?,
                body: ByteArray?
            ) {
                super.handleDelivery(consumerTag, envelope, properties, body)
                body?.let {
                    Logger.d("handleDelivery: ${String(it)}")
                }
            }
        }
        channel.basicConsume("serverInfo", true, consumer)
    }
    thread.start()
}

val basicProperties by lazy {
    AMQP.BasicProperties.Builder()
        .contentType("text/plain")
        .deliveryMode(2)
        .priority(1)
        .build()
}