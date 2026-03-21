package com.example.clouduser.controller;

import org.example.client.orderClient.TestOrderClient;
import org.example.client.productClient.TestProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务测试控制器
 */
@RestController()
@RequestMapping("/v1")
public class TestController {
    @Autowired
    private TestOrderClient testOrderClient;
    
    @Autowired
    private TestProductClient testProductClient;

    @PostMapping("/get")
    public String get(){return "user";}

    @PostMapping("/call-order")
    public String callOrder(){return testOrderClient.get();}
    
    @PostMapping("/call-product")
    public String callProduct(){return testProductClient.get();}
}
