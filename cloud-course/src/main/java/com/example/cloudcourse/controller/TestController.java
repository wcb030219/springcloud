package com.example.cloudcourse.controller;

import org.example.client.evaluationClient.TestEvaluationClient;
import org.example.client.userClient.TestUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程服务测试控制器
 */

@RestController()
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private TestUserClient testUserClient;
    
    @Autowired
    private TestEvaluationClient testEvaluationClient;

    @PostMapping("/get")
    public String get(){return "course";}
    
    @PostMapping("/call-user")
    public String callUser(){return testUserClient.get();}
    
    @PostMapping("/call-evaluation")
    public String callEvaluation(){return testEvaluationClient.get();}
}
