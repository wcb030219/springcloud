package org.example.client.userClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 用户服务Feign客户端
 */
@FeignClient("cloudUser")
@RequestMapping("/user/v1")
public interface TestUserClient {
    @PostMapping("/get")
    public String get();
    
    @PostMapping("/call-order")
    public String callOrder();
    
    @PostMapping("/call-product")
    public String callProduct();
}
