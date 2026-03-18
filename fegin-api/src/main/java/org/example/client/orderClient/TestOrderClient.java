package org.example.client.orderClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: poet_Dai
 * @create: 2024-11-23 16:24
 * @Description:
 */
@FeignClient("cloudOrder")
@RequestMapping("/order/v1")
public interface TestOrderClient {
    @PostMapping("/get")
    public String get();
}
