package com.mianzi.showticketsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 启用定时任务支持
public class ShowTicketSystemApplication {

    public static void main(String[] args) {
        //固定结构,用于运行SpringBoot项目
        SpringApplication.run(ShowTicketSystemApplication.class, args);
    }

}
