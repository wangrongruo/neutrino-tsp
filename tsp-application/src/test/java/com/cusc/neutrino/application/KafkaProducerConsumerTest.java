package com.cusc.neutrino.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
public class KafkaProducerConsumerTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaTestListener listener;

    private static final String TEST_TOPIC = "neutrino-connection-test";

    @Test
    void testSendAndReceive() throws InterruptedException {
        log.info("开始测试Kafka连接...");
        String message = "Hello, Kafka from Neutrino-TSP test!";
        
        log.info("向主题 '{}' 发送消息: {}", TEST_TOPIC, message);
        kafkaTemplate.send(TEST_TOPIC, message);

        // 等待消费者接收消息，设置5秒超时
        boolean messageReceived = listener.getLatch().await(5, TimeUnit.SECONDS);

        assertTrue(messageReceived, "在5秒内没有接收到Kafka消息");
        log.info("Kafka生产者和消费者测试成功！");
    }

    @Component
    public static class KafkaTestListener {
        private CountDownLatch latch = new CountDownLatch(1);
        private String payload;

        @KafkaListener(topics = KafkaProducerConsumerTest.TEST_TOPIC, groupId = "test-consumer-group")
        public void receive(ConsumerRecord<String, String> record) {
            log.info("测试消费者接收到消息: {}", record.value());
            this.payload = record.value();
            latch.countDown();
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public String getPayload() {
            return payload;
        }
    }
}