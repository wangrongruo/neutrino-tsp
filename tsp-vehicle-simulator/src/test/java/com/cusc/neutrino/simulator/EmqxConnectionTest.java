package com.cusc.neutrino.simulator;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;

import java.util.UUID;


@Slf4j
public class EmqxConnectionTest {

    // **重要：请将IP替换为您的服务器公网IP**
    private static final String BROKER_URL = "tcp://47.99.70.238:1883";
    private static final String CLIENT_ID = "test-client-" + UUID.randomUUID();

    @Test
    public void testMqttConnection() {
        log.info("开始测试EMQX (MQTT) 连接...");
        MqttClient client = null;
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            log.info("正在连接到Broker: {}", BROKER_URL);
            client.connect(connOpts);
            log.info("连接成功！");
            if (!client.isConnected()) {
                log.error("MQTT客户端未能成功连接");
            }

            client.disconnect();
            log.info("断开连接成功！");

        } catch (MqttException me) {
            log.error("MQTT连接测试失败", me);
            throw new RuntimeException(me);
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    // ignore
                }
            }
        }
    }
}