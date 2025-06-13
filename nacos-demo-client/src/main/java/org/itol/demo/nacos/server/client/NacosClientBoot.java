package org.itol.demo.nacos.server.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class NacosClientBoot {
    public static void main(String[] args) {
        SpringApplication.run(NacosClientBoot.class, args);
    }
}
