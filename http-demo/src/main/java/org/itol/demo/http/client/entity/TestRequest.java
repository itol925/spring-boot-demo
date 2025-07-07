package org.itol.demo.http.client.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class TestRequest implements Serializable {
    // 1 http.post
    // 2 http.get
    // 3 websocket
    private Integer type;
    private String url;
    private Map<String, Object> payload;
    private Map<String, Object> header;
    private Integer duration;
    private Integer vus;
    private Integer maxVus;
    private List<Integer> qpsList;
    private String outDir;
}
