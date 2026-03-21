package org.example.client.evaluationClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 评估服务Feign客户端
 */
@FeignClient("cloudEvaluation")
@RequestMapping("/evaluation/v1")
public interface TestEvaluationClient {
    @PostMapping("/get")
    public String get();
    
    @PostMapping("/call-user")
    public String callUser();
    
    @PostMapping("/call-course")
    public String callCourse();
}
