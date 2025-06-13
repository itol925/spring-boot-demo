package org.itol.demo.nacos.server.client.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 注：boot 里需要 @EnableFeignClients
 */
@FeignClient("nacos-demo-service")
public interface TestOpenFeign {
    @GetMapping("/hello")
    String hello();
}
