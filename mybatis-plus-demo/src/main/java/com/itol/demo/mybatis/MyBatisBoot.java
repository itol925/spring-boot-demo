package com.itol.demo.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.itol.demo.mybatis.mapper")
public class MyBatisBoot {

    public static void main(String[] args) {
        SpringApplication.run(MyBatisBoot.class, args);
    }

}