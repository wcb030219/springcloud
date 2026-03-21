package org.example.client.courseClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 课程服务Feign客户端
 */
@FeignClient("cloudCourse")
@RequestMapping("/course/v1")
public interface TestCourseClient {
    @PostMapping("/get")
    public String get();
    
    @PostMapping("/call-user")
    public String callUser();
    
    @PostMapping("/call-product")
    public String callProduct();
}
