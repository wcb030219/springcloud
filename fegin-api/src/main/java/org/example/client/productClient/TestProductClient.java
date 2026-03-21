package org.example.client.productClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 产品服务Feign客户端
 */
@FeignClient("cloudProduct")
@RequestMapping("/product/v1")
public interface TestProductClient {
    @PostMapping("/get")
    public String get();
    
    @PostMapping("/call-user")
    public String callUser();
    
    @PostMapping("/call-order")
    public String callOrder();
}
