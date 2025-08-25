package com.cusc.neutrino.application.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 一个简单的健康检查控制器，用于验证核心服务的连通性
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    // 注入JdbcTemplate，这是执行简单SQL查询最便捷的方式
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 测试与主数据源(dbUser)的连接
     *
     * @return 包含状态信息的Map
     */
    @GetMapping("/db")
    @DS("dbUser") // **核心：明确指定使用名为"dbUser"的数据源**
    public Map<String, Object> checkDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        try {
            // 执行一个最简单的查询
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            if (result != null && result == 1) {
                response.put("status", "UP");
                response.put("message", "Successfully connected to the primary database (dbUser).");
            } else {
                response.put("status", "DOWN");
                response.put("message", "Query executed, but the result was unexpected.");
            }
        } catch (Exception e) {
            log.error("Failed to connect to the database: ", e);
            System.out.println(e);
            response.put("status", "DOWN");
            response.put("message", "Failed to connect to the database.");
            // 在服务器日志中打印详细的异常信息，便于排查
            response.put("error", e.getMessage());
        }
        return response;
    }
}