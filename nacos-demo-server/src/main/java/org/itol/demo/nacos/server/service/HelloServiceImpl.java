package org.itol.demo.nacos.server.service;

import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService {
    private int count;
    @Override
    public String hello() {
        return String.format("hello %d", count++);
    }
}
