package org.example.client.orderClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 订单服务Feign客户端
 */
@FeignClient("cloudOrder")
@RequestMapping("/order/v1")
public interface TestOrderClient {
    @PostMapping("/get")
    public String get();
    
    @PostMapping("/call-user")
    public String callUser();
    
    @PostMapping("/call-product")
    public String callProduct();
}
