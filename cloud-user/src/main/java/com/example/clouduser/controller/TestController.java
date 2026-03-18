package com.example.clouduser.controller;

import org.example.client.orderClient.TestOrderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: poet_Dai
 * @create: 2024-11-23 16:18
 * @Description:
 */
@RestController()
@RequestMapping("/v1")
public class TestController {
    @Autowired
    private TestOrderClient testOrderClient;

    @PostMapping("/get")
    public String get(){
        return "user";
    }

    @PostMapping("/call-order")
    public String callOrder(){
        return testOrderClient.get();
    }
}
