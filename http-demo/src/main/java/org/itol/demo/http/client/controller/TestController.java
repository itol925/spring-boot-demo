package org.itol.demo.http.client.controller;

import lombok.extern.slf4j.Slf4j;
import org.itol.demo.http.client.utils.K6TestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
public class TestController {
    private int curReportId = 0;
    private final Map<Integer, String> reportMap = new HashMap<>();
    private final int capacity = 20;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicInteger runningTasks = new AtomicInteger(0);

    @PostMapping("/k6/hello")
    public String hello() {
        return "hello k6";
    }

    @PostMapping("/k6/http/test")
    public String testHttpPost(@RequestBody K6TestUtils.TestOption option) {
        if (runningTasks.get() > 0) {
            return "任务正在执行中，请稍后再试";
        }
        System.out.println("Submitting task...");
        runningTasks.incrementAndGet();
        curReportId++;

        executor.submit(() -> {
            System.out.println("Task started");
            try {
                System.out.println("run task...");
                String report = K6TestUtils.testAPI(option);
                reportMap.put(curReportId, report);
                if (reportMap.size() > capacity) {
                    removeExcess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                reportMap.put(curReportId, e.getMessage());
            } finally {
                System.out.println("Task finished");
                runningTasks.decrementAndGet();
            }
        });
        System.out.println("Submitted.");
        return "压测任务启动成功. reportId:" + curReportId;
    }

    @PostMapping("/k6/http/testNow")
    public String testHttpPostNow(@RequestBody K6TestUtils.TestOption option) {
        return K6TestUtils.testAPI(option);
    }

    @PostMapping("/k6/install")
    public String testInstallK6() {
        return K6TestUtils.installK6();
    }

    @GetMapping("k6/http/report/{id}")
    public String getTaskReport(@PathVariable("id") String id) {
        Integer reportId = Integer.parseInt(id);
        if (reportMap.containsKey(reportId)) {
            return reportMap.get(reportId);
        }
        return "报告没有生成，或者已经移除了";
    }

    @GetMapping("k6/http/task")
    public String getTaskStatus() {
        List<Integer> reportIds = new ArrayList<>(reportMap.keySet());
        Collections.sort(reportIds);
        if (runningTasks.get() > 0) {
            return "当前状态：正在执行任务reportId:" + curReportId + "\n" +
                    "本地报告列表：" + reportIds;
        }
        return "当前状态：空闲"+ "\n" +
                "本地报告列表：" + reportIds;
    }

    private void removeExcess() {
        Integer removeId = curReportId - capacity;
        while (removeId > 0 && reportMap.containsKey(removeId)) {
            reportMap.remove(removeId);
            removeId--;
        }
    }
}
