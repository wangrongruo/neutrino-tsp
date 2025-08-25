package com.cusc.neutrino.application;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan({"com.cusc.neutrino.**"})
@ComponentScan({"com.cusc.neutrino.**"})
@Slf4j
public class NeutrinoTSPApplication {
    public static void main(String[] args) {
        try {

            ApplicationContext context = SpringApplication.run(NeutrinoTSPApplication.class, args);
            String serverPort = context.getEnvironment().getProperty("server.port");
            log.info("启动 Swagger2: http://127.0.0.1:" + serverPort + "/swagger-ui.html");
        } catch (Exception ex) {
            // **这是关键：捕获所有类型的异常，包括Error**
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("!!               SPRING BOOT FAILED TO START               !!");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            // 打印最根本的、最详细的异常堆栈
            ex.printStackTrace();

            // 以一个非零的错误码退出，明确告诉系统是异常终止
            System.exit(1);
        }
    }
}
