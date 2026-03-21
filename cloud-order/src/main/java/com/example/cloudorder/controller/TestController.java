package com.example.cloudorder.controller;

import org.example.client.productClient.TestProductClient;
import org.example.client.userClient.TestUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单服务测试控制器
 */

@RestController()
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private TestUserClient testUserClient;
    
    @Autowired
    private TestProductClient testProductClient;

    @PostMapping("/get")
    public String get(){return "order";}
    
    @PostMapping("/call-user")
    public String callUser(){return testUserClient.get();}
    
    @PostMapping("/call-product")
    public String callProduct(){return testProductClient.get();}
}
