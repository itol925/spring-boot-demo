package org.itol.demo.http.client.controller;

import org.itol.demo.http.client.utils.K6TestUtils;
import org.itol.demo.http.client.entity.TestRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("k6")
public class K6Controller {

    @PostMapping("/http/test")
    public String testHttpPost(@RequestBody TestRequest request) {
        return K6TestUtils.testAPI(convert(request));
    }

    K6TestUtils.TestOption convert(TestRequest request) {
        K6TestUtils.TestOption option = new K6TestUtils.TestOption();
        option.setType(request.getType());
        option.setUrl(request.getUrl());
        option.setPayload(request.getPayload());
        option.setHeader(request.getHeader());
        option.setDuration(request.getDuration());
        option.setVus(request.getVus());
        option.setMaxVus(request.getMaxVus());
        option.setQpsList(request.getQpsList());
        option.setOutDir(request.getOutDir());
        return option;
    }
}
