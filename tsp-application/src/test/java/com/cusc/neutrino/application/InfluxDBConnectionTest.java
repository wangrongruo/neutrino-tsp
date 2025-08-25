package com.cusc.neutrino.application;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.HealthCheck;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class InfluxDBConnectionTest {


    @Value("${influxdb.url}")
    private String influxUrl;
    @Value("${influxdb.token}")
    private String token;
    @Value("${influxdb.org}")
    private String org;
    @Value("${influxdb.bucket}")
    private String bucket;

    private InfluxDBClient influxDBClient;

    // **核心修正：使用 @BeforeEach 注解，并将方法改为非静态**
    @BeforeEach
    void setUp() {
        System.out.println("初始化InfluxDB客户端, 连接到: {}" + influxUrl);
        // 现在，在非静态方法中，可以自由访问所有非静态成员变量了
        influxDBClient = InfluxDBClientFactory.create(influxUrl, token.toCharArray(), org, bucket);
    }

    // **核心修正：使用 @AfterEach 来对应 @BeforeEach**
    @AfterEach
    void tearDown() {
        if (influxDBClient != null) {
            influxDBClient.close();
            System.out.println("关闭InfluxDB客户端。");
        }
    }

    @Test
    void testHealthCheck() {
        System.out.println("开始测试InfluxDB健康检查...");
        HealthCheck health = influxDBClient.health();
        System.out.println("健康检查状态: {}" + health.getStatus());
        assertEquals(HealthCheck.StatusEnum.PASS, health.getStatus(), "InfluxDB健康检查未通过");
        System.out.println("InfluxDB健康检查成功！");
    }

    @Test
    void testWritePoint() {
        System.out.println("开始测试InfluxDB写入数据点...");
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement("connection_test")
                .addTag("host", "test-host")
                .addField("value", 1.0)
                .time(Instant.now(), WritePrecision.NS);

        writeApi.writePoint(point);
        System.out.println("数据点写入成功！");
    }
}