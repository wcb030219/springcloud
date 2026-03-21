package com.example.clouduser.controller;

import org.example.client.courseClient.TestCourseClient;
import org.example.client.evaluationClient.TestEvaluationClient;
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
    private TestCourseClient testCourseClient;
    
    @Autowired
    private TestEvaluationClient testEvaluationClient;

    @PostMapping("/get")
    public String get(){return "user";}

    @PostMapping("/call-course")
    public String callCourse(){return testCourseClient.get();}
    
    @PostMapping("/call-evaluation")
    public String callEvaluation(){return testEvaluationClient.get();}
}
