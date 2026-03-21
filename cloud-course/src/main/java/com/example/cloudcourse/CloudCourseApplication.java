package com.example.cloudcourse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.example.client")
@MapperScan("com.example.cloudcourse.mapper")
public class CloudCourseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudCourseApplication.class, args);
    }

}
