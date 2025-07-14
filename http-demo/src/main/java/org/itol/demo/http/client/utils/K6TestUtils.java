package org.itol.demo.http.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @desc k6压测工具，支持http get, http post, websocket 3种类型的接口压测
 * 输入TestOption，参数参考类的字段
 * 输出请求的延时报告，含平均，p95, p99延时数据
 * 需要施压机安装k6
 * @auth panyinglong
 */
public class K6TestUtils {
    private final static ObjectMapper mapper = new ObjectMapper();
    public final static Integer HTTP_POST = 1;
    public final static Integer HTTP_GET = 2;
    public final static Integer WEBSOCKET = 3;

    public static String testAPI(TestOption option) {
        File shellScript = null;
        File jsScript = null;
        try {
            shellScript = createTempFile(shellScript(), ".sh");
            if (HTTP_POST.equals(option.getType())) {
                jsScript = createTempFile(httpPostScript(), ".js");
            } else if (HTTP_GET.equals(option.getType())) {
                jsScript = createTempFile(httpGetScript(), ".js");
            } else if (WEBSOCKET.equals(option.getType())) {
                jsScript = createTempFile(webSocketScript(), ".js");
            } else {
                return "error! unknown test type";
            }

            List<String> commands = getCommands(option, shellScript.getAbsolutePath(), jsScript.getAbsolutePath());
            return executeShell(commands);
        } catch (Exception e) {
            return "执行异常: " + e.getMessage();
        } finally {
            if (shellScript != null) {
                shellScript.deleteOnExit();
            }
            if (jsScript != null) {
                jsScript.deleteOnExit();
            }
        }
    }

    public static String installK6() {
        File shellScript = null;
        try {
            shellScript = createTempFile(installK6Script(), ".sh");
            List<String> commands = Arrays.asList("bash", shellScript.getAbsolutePath());
            return executeShell(commands);
        } catch (Exception e) {
            return "执行异常: " + e.getMessage();
        } finally {
            if (shellScript != null) {
                shellScript.deleteOnExit();
            }
        }
    }

    private static List<String> getCommands(TestOption option, String shellPath, String scriptPath) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("bash");
        command.add(shellPath);
        command.add(scriptPath); // #1 第一个参数

        if (!StringUtils.hasText(option.getAddr())) {
            throw new IllegalArgumentException("addr is empty");
        }
        command.add(option.getAddr()); // #2

        if (CollectionUtils.isEmpty(option.getRequests())) {
            throw new IllegalArgumentException("request is empty");
        }
        int totalRatio = option.getRequests().get(0).ratio;
        for (int i = 1; i < option.getRequests().size(); i++) {
            int addRatio = option.getRequests().get(i).ratio;
            option.getRequests().get(i).ratio += totalRatio;
            totalRatio += addRatio;
        }
        command.add(mapper.writeValueAsString(option.getRequests())); // #3

        if (option.getDuration() == null) {
            throw new IllegalArgumentException("duration is null");
        }
        command.add(option.getDuration().toString()); // #4

        if (option.getVus() == null) {
            throw new IllegalArgumentException("vu（虚拟用户数） is null");
        }
        command.add(option.getVus().toString()); // #5

        if (option.getMaxVus() == null) {
            option.setMaxVus(option.getVus());
        }
        command.add(option.getMaxVus().toString()); // #6

        String qpsList = "";
        for (int i = 0; i < option.getQps().size(); i++) {
            qpsList += option.getQps().get(i).toString();
            if (i < option.getQps().size() - 1) {
                qpsList += " ";
            }
        }
        if (qpsList.equals("")) {
            throw new IllegalArgumentException("qps is empty");
        }
        command.add(qpsList); // #7

        command.add(option.getOutDir()); // #8
        return command;
    }

    private static String executeShell(List<String> commands) {
        try {
            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true); // 合并 stdout 和 stderr
            Process process = builder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            try {
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return "执行成功:\n" + output;
                } else {
                    return "执行失败 (exitCode=" + exitCode + "):\n" + output;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return "执行异常：" + ie.getMessage();
            }
        } catch (Exception e) {
            return "执行异常: " + e.getMessage();
        }
    }

    private static File createTempFile(String script, String fileSuffix) throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile("k6-script", fileSuffix);
        tempFile.deleteOnExit();

        // 写入脚本内容到临时文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(script);
        }

        // 对 .sh 脚本加执行权限
        if (fileSuffix.endsWith(".sh")) {
            tempFile.setExecutable(true);
        }

        return tempFile;
    }

    private static String httpGetScript() {
        return ResourceReader.read("scripts/http-get.js");
    }

    private static String httpPostScript() {
        return ResourceReader.read("scripts/http-post.js");
    }

    private static String webSocketScript() {
        return ResourceReader.read("scripts/websocket.js");
    }

    private static String shellScript() {
        return ResourceReader.read("scripts/k6-run.sh");
    }

    private static String installK6Script() {
        return """
            #!/bin/bash
                        
            # 检查是否已安装 k6
            if command -v k6 &> /dev/null; then
                echo "k6 已安装，当前版本：$(k6 version | head -n 1)"
                exit 0
            fi
            
            # 检查是否为 root 用户
            if [ "$(id -u)" -ne 0 ]; then
                echo "请使用 root 用户或 sudo 运行此脚本！"
                exit 1
            fi
                        
            # 添加 k6 的 RPM 仓库
            echo "正在添加 k6 的 RPM 仓库..."
            rpm -ivh https://dl.k6.io/rpm/repo.rpm
                        
            # 安装 k6
            echo "正在安装 k6..."
            if command -v dnf &> /dev/null; then
                dnf install -y k6
            elif command -v yum &> /dev/null; then
                yum install -y k6
            else
                echo "错误：未找到 yum 或 dnf 包管理器！"
                exit 1
            fi
                        
            # 验证安装
            echo "验证 k6 是否安装成功..."
            if k6 version &> /dev/null; then
                echo "k6 安装成功！当前版本：$(k6 version | head -n 1)"
            else
                echo "k6 安装失败，请检查错误信息！"
                exit 1
            fi
            """;
    }

    @Data
    public static class TestOption implements Serializable {
        /**
         * 请求类型
         * 1 http post 接口
         * 2 http get 接口
         * 3 websocket 接口
         */
        private Integer type;

        /**
         * 请求的url
         * eg http post：http://127.0.0.1:10030
         * eg http get: http://127.0.0.1:10030
         * eg websocket: ws://127.0.0.1:11000
         */
        private String addr;

        /**
         * 请求参数，包括payload 和 header
         * eg: {"OrderSysID":"1000565760833093"}
         * eg: {"RequestNo":2,"SendQryOrder":{"OrderID":"1000565760833093"}}
         */
        private List<Request> requests;

        /**
         * 压测持续时间
         * 单位：秒
         * 每个qps场景压测的时长，n个qps的话压测总时长为n*duration
         */
        private Integer duration;

        /**
         * 虚拟用户数
         */
        private Integer vus;

        /**
         * 最大虚拟用户数
         */
        private Integer maxVus;

        /**
         * 压测qps
         * 比如qps为100，vu为2，那每个vu分配到的qps为50
         * 支持输入一个qps list，每个qps压测duration时长
         */
        private List<Integer> qps;

        /**
         * 输出报告目录
         * 后续接grafana的话可能需要用到
         * 为空的话，报告输出到控制台，内容由testAPI函数以string类型返回
         */
        private String outDir;

        @Data
        public static class Request implements Serializable {
            /**
             * 执行频率
             */
            private Integer ratio;
            /**
             * 请求的path
             * eg http post：/action/v1.0/SendQryOne/Order
             * eg http get: /query/v1.0/CanceledOrder?MemberID=004a05ee-d7e6-44bb-b238-3feb205de1d0
             * eg websocket: 不需要path参数
             */
            private String path;
            /**
             * 请求体
             * eg: {"OrderSysID":"1000565760833093"}
             * eg: {"RequestNo":2,"SendQryOrder":{"OrderID":"1000565760833093"}}
             */
            private Map<String, Object> payload;

            /**
             * 请求头
             * eg: {"Content-Type":"application/json"}
             */
            private Map<String, Object> header;
        }
    }
}
