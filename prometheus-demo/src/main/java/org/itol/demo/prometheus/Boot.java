package org.itol.demo.prometheus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Slf4j
@SpringBootApplication
public class Boot {
    public static void main(String[] args) {
//        SpringApplication.run(Boot.class, args);
        // 这里强制以 WebFlux 方式启动，因为 Spring Boot 项目默认进入 Servlet 模式（Spring MVC），而不是 Reactive 模式（WebFlux）
        // filter 也默认是 javax.servlet.Filter，不是 WebFilter
        new SpringApplicationBuilder(Boot.class)
                .web(WebApplicationType.REACTIVE)
                .run(args);
    }
}
