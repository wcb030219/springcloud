package com.example.cloudevaluation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.example.client")
@MapperScan("com.example.cloudevaluation.mapper")
public class CloudEvaluationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudEvaluationApplication.class, args);
    }

}
