package com.example.cloudproduct.controller;

import org.example.client.orderClient.TestOrderClient;
import org.example.client.userClient.TestUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品服务测试控制器
 */

@RestController()
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private TestUserClient testUserClient;
    
    @Autowired
    private TestOrderClient testOrderClient;

    @PostMapping("/get")
    public String get(){return "product";}
    
        @PostMapping("/call-user")
    public String callUser(){return testUserClient.get();}
    
    @PostMapping("/call-order")
    public String callOrder(){return testOrderClient.get();}
}
