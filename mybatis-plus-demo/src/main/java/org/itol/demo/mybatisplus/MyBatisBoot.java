package org.itol.demo.mybatisplus;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan("org.itol.demo.mybatisplus.mapper")
public class MyBatisBoot {

    public static void main(String[] args) {
        SpringApplication.run(MyBatisBoot.class, args);
    }

}