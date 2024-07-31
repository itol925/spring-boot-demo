package org.itol.demo.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.itol.demo.mybatisplus.mapper")
public class MyBatisBoot {

    public static void main(String[] args) {
        SpringApplication.run(MyBatisBoot.class, args);
    }

}