package com.bilibili;

import com.bilibili.service.websocket.WebSocketService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@MapperScan("com.bilibili.dao")
@SpringBootApplication
public class FakeBilibiliApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(FakeBilibiliApp.class, args);
        WebSocketService.initService(context);
    }

}
