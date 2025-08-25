package com.cusc.neutrino.application;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
public class MySqlMyBatisPlusTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testDataSourceConnection() throws SQLException {
        log.info("开始测试数据库连接...");
        assertNotNull(dataSource, "数据源不应为null");

        try (Connection connection = dataSource.getConnection()) {
            log.info("成功获取数据库连接: {}", connection);
            assertNotNull(connection, "数据库连接不应为null");
            log.info("数据库连接测试成功！");
        }
    }

    @Test
    void testJdbcTemplate() {
        log.info("开始测试JdbcTemplate查询...");
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        log.info("查询结果: {}", result);
        assert (result == 1);
        log.info("JdbcTemplate查询测试成功！");
    }
}