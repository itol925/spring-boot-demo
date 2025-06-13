package org.itol.demo.prometheus.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.concurrent.TimeUnit;

@Component
public class MyWebFilter implements WebFilter {
    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        System.out.println("init");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        System.out.println("filter");
        long requestStart = System.nanoTime();
        String path = exchange.getRequest().getPath().toString();
        return Mono.defer(() -> {
            long businessStart = System.nanoTime();

            return chain.filter(exchange)
                .doFinally(signalType -> {
                    if (signalType == SignalType.CANCEL) {
                        return; // 可选：不记录取消
                    }
                    long end = System.nanoTime();
                    recordMetrics(exchange, path,  businessStart, requestStart, end);
                });
        });
    }

    private void recordMetrics(ServerWebExchange exchange, String path, long requestStart, long businessStart, long end) {
        long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(end - requestStart);
        long businessTimeMs = TimeUnit.NANOSECONDS.toMillis(end - businessStart);

        Timer.builder("http_total_time")
                .publishPercentiles(0.5, 0.95, 0.99) // 添加百分位支持
                .publishPercentileHistogram()        // 同时开启 histogram（有利于直方图+Grafana）
                .tags("uri", path, "method", exchange.getRequest().getMethod().toString(), "status", getStatus(exchange))
                .register(meterRegistry).record(totalTimeMs, TimeUnit.MILLISECONDS);

        Timer.builder("http_business_time")
                .publishPercentiles(0.5, 0.95, 0.99) // 添加百分位支持
                .publishPercentileHistogram()        // 同时开启 histogram（有利于直方图+Grafana）
                .tags("uri", path, "method", exchange.getRequest().getMethod().toString(), "status", getStatus(exchange))
                .register(meterRegistry).record(businessTimeMs, TimeUnit.MILLISECONDS);
    }

    private String getStatus(ServerWebExchange exchange) {
        if (exchange.getResponse().getStatusCode() != null) {
            return String.valueOf(exchange.getResponse().getStatusCode().value());
        } else {
            return "UNKNOWN";
        }
    }
}
