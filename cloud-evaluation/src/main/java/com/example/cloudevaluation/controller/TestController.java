package com.example.cloudevaluation.controller;

import org.example.client.courseClient.TestCourseClient;
import org.example.client.userClient.TestUserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评估服务测试控制器
 */

@RestController()
@RequestMapping("/v1")
public class TestController {

    @Autowired
    private TestUserClient testUserClient;
    
    @Autowired
    private TestCourseClient testCourseClient;

    @PostMapping("/get")
    public String get(){return "evaluation";}
    
        @PostMapping("/call-user")
    public String callUser(){return testUserClient.get();}
    
    @PostMapping("/call-course")
    public String callCourse(){return testCourseClient.get();}
}
