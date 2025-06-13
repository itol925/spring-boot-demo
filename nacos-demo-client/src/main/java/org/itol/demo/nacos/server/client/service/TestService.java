package org.itol.demo.nacos.server.client.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

@Service
public class TestService {
    private final RestTemplate template = new RestTemplate();

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private TestOpenFeign testOpenFeign;

    @PostConstruct
    public void init() {
        helloUseDiscovery();

        helloUseOpenFeign();
    }


    private void helloUseDiscovery() {
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances("nacos-demo-service");
        if (CollectionUtils.isEmpty(serviceInstanceList)) {
            throw new RuntimeException("no available service:nacos-demo-service");
        }
        ServiceInstance instance = serviceInstanceList.get(new Random().nextInt(serviceInstanceList.size()));
        ResponseEntity<String> response = template.exchange(instance.getUri() + "/hello", HttpMethod.GET, null, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            String hello = response.getBody();
            System.out.println("succeed:" + hello);
        } else {
            System.out.println("failed");
        }
    }

    private void helloUseOpenFeign() {
        try {
            String hello = testOpenFeign.hello();
            System.out.println("succeed:" + hello);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
