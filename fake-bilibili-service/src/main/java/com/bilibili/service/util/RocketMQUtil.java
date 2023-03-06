package com.bilibili.service.util;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocketMQUtil {
    static final Logger logger = LoggerFactory.getLogger(RocketMQUtil.class);
    public static void syncSendMsg(DefaultMQProducer producer, Message msg) throws Exception{
        SendResult result = producer.send(msg);
        logger.debug("send msg to rocketMQ, sendStatus: {}", result.getSendStatus());
    }

    public static void asyncSendMsg(DefaultMQProducer producer, Message msg) throws Exception{
        // DefaultMQProducer是线程安全的
        producer.send(msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("异步发送消息成功，消息id：" + sendResult.getMsgId());
            }
            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
