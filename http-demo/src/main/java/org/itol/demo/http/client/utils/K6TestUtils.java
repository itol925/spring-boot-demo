package org.itol.demo.http.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
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
        command.add(scriptPath);
        if (!StringUtils.hasText(option.getUrl())) {
            throw new IllegalArgumentException("url is empty");
        }
        command.add(option.getUrl());
        command.add(mapper.writeValueAsString(option.getPayload()));
        command.add(mapper.writeValueAsString(option.getHeader()));
        if (option.getDuration() == null) {
            throw new IllegalArgumentException("duration is null");
        }
        command.add(option.getDuration().toString() + "s");
        if (option.getVus() == null) {
            throw new IllegalArgumentException("vu（虚拟用户数） is null");
        }
        command.add(option.getVus().toString());
        if (option.getMaxVus() == null) {
            option.setMaxVus(option.getVus());
        }
        command.add(option.getMaxVus().toString());
        String qpsList = "";
        for (int i = 0; i < option.getQpsList().size(); i++) {
            qpsList += option.getQpsList().get(i).toString();
            if (i < option.getQpsList().size() - 1) {
                qpsList += " ";
            }
        }
        if (qpsList.equals("")) {
            throw new IllegalArgumentException("qps is empty");
        }
        command.add(qpsList);
        command.add(option.getOutDir());
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
        return """
             import http from 'k6/http';
             import { check } from 'k6';
             import { Trend } from 'k6/metrics';
                            
             // 读取环境变量参数
             const url = __ENV.TARGET_URL;
             const payload = __ENV.PAYLOAD;
                            
             let headers = {
                 'Content-Type': 'application/json',
             };
             if (__ENV.HEADERS) {
                 try {
                     headers = JSON.parse(__ENV.HEADERS);
                 } catch (e) {
                     console.error('HEADERS 不是有效的 JSON');
                 }
             }
                            
             const QPS = Number(__ENV.QPS || '100');
             const DURATION = __ENV.DURATION || '10s';
             const VUS = Number(__ENV.VUS || '100');
             const MAX_VUS = Number(__ENV.MAX_VUS || '200');
                            
             const latencyTrend = new Trend('http_get_latency');
             // 配置场景
             export const options = {
                 scenarios: {
                     dynamic_qps: {
                         executor: 'constant-arrival-rate',
                         rate: QPS,
                         timeUnit: '1s',
                         duration: DURATION,
                         preAllocatedVUs: VUS,
                         maxVUs: MAX_VUS,
                     },
                 },
                 thresholds: {
                     'http_get_latency': ['p(95)<1000', 'p(99)<2000'],
                 },
             };
                            
             export default function () {
                 const res = http.get(url, { "headers" : headers });
                 // console.log(JSON.stringify(res))
                 latencyTrend.add(res.timings.duration);
                 const msg = JSON.parse(res.body);
                 check(msg, { 'errorCode = 0': (m) => m && m.errorCode === 0 });
                 check(res, {
                     'status = 200': (r) => r.status === 200,
                 });
             }
            """;
    }

    private static String httpPostScript() {
        return """
             import http from 'k6/http';
             import { check } from 'k6';
             import { Trend } from 'k6/metrics';
                            
             // 读取环境变量参数
             const url = __ENV.TARGET_URL;
             const payload = __ENV.PAYLOAD;
                            
             let headers = {
                 'Content-Type': 'application/json',
             };
             if (__ENV.HEADERS) {
                 try {
                     headers = JSON.parse(__ENV.HEADERS);
                 } catch (e) {
                     console.error('HEADERS 不是有效的 JSON');
                 }
             }
                            
             const QPS = Number(__ENV.QPS || '100');
             const DURATION = __ENV.DURATION || '10s';
             const VUS = Number(__ENV.VUS || '100');
             const MAX_VUS = Number(__ENV.MAX_VUS || '200');
                            
             const latency = new Trend('custom_latency');
             // 配置场景
             export const options = {
                 scenarios: {
                     dynamic_qps: {
                         executor: 'constant-arrival-rate',
                         rate: QPS,
                         timeUnit: '1s',
                         duration: DURATION,
                         preAllocatedVUs: VUS,
                         maxVUs: MAX_VUS,
                     },
                 },
                 thresholds: {
                     'custom_latency': ['p(95)<1000', 'p(99)<2000'],
                 },
             };
                            
             export default function () {
                 const res = http.post(url, payload, { headers });
                 const msg = JSON.parse(res.body);
                 check(msg, { 'errorCode = 0': (m) => m && m.errorCode === 0 });
                 //console.log('--------' + msg.errorCode);
                 latency.add(res.timings.duration);
                 check(res, {
                     'status = 200': (r) => r.status === 200,
                 });
             }
             """;
    }

    private static String webSocketScript() {
        return """
             import ws from 'k6/ws';
             import { check } from 'k6';
             import { Trend } from 'k6/metrics';
                            
             export const options = {
                 scenarios: {
                     websocket_load: {
                         executor: 'per-vu-iterations',
                         vus: parseInt(__ENV.VUS || '1'),
                         iterations: 1,
                         maxDuration: __ENV.DURATION || '10s',
                     }
                 }
             };
                            
             const URL = __ENV.TARGET_URL;
             const VUS = parseInt(__ENV.VUS || '1');
             const QPS = parseInt(__ENV.QPS || '1');
             const PAYLOAD_TEMPLATE = __ENV.PAYLOAD;
                            
             // 统计延迟的趋势对象
             const latencyTrend = new Trend('ws_latency', true);
                            
             export default function () {
                 console.log("--------- run default function")
                 const connectionQPS = QPS / VUS;
                 const intervalSeconds = 1 / connectionQPS;
                            
                 const res = ws.connect(URL, {}, function (socket) {
                     const sendTimes = new Map();
                     let seq = 0;
                            
                     socket.on('open', () => {
                         console.log(`VU ${__VU} 已连接, intervalSeconds=` + intervalSeconds);
                         // 设置定时发送消息
                         socket.setInterval(() => {
                             const requestNo = seq++;
                             const payload = injectRequestNo(PAYLOAD_TEMPLATE, requestNo);
                            
                             sendTimes.set(requestNo, Date.now());
                             socket.send(payload);
                             // console.log("send << " + requestNo);
                         }, intervalSeconds * 1000); // 发送间隔（秒）
                            
                         socket.setTimeout(() => {
                             socket.close();
                             console.log(`VU ${__VU} 超时关闭连接`);
                         }, 5000);
                     });
                            
                     // 接收响应并记录延迟
                     socket.on('message', (data) => {
                         try {
                             const msg = JSON.parse(data);
                             check(msg, { 'errorCode = 0': (m) => m && m.errorCode === 0 });
                            
                             const reqNo = msg.requestNo;
                             // console.log("recv >> " + reqNo);
                             const sendTime = sendTimes.get(reqNo);
                             if (sendTime) {
                                 const latency = Date.now() - sendTime;
                                 latencyTrend.add(latency);
                                 sendTimes.delete(reqNo);
                             } else {
                                 // console.warn(`未找到 RequestNo: ${reqNo}`);
                             }
                         } catch (e) {
                             console.error(`响应解析失败: ${e.message}`);
                         }
                     });
                            
                     socket.on('close', () => {
                         console.log(`VU ${__VU} 连接已关闭 reqNo:` + seq);
                     });
                            
                     socket.on('error', (e) => {
                         console.error(`VU ${__VU} 连接出错: ${e.error()}`);
                     });
                            
                     socket.setInterval(() => {
                         const now = Date.now();
                         const TTL = 30000; // 超过 30s 的记录就清掉
                            
                         for (const [reqNo, sendTime] of sendTimes.entries()) {
                             if (now - sendTime > TTL) {
                                 sendTimes.delete(reqNo);
                             }
                         }
                     }, 5000); // 发送间隔（秒）
                 });
                            
                 check(res, { 'status = 101': (r) => r && r.status === 101 });
             }
             // 将 payload 插入 RequestNo 字段
             function injectRequestNo(payload, requestNo) {
                 let obj = {};
                 try {
                     obj = JSON.parse(payload);
                 } catch (e) {
                     console.error("payload 不是合法 JSON");
                 }
                 obj.RequestNo = requestNo;
                 return JSON.stringify(obj);
             }
             """;
    }

    private static String shellScript() {
        return """
             #!/bin/bash
                        
             SCRIPT=$1
             URL=$2
             PAYLOAD=$3
             HEADERS=$4
             DURATION=$5
             VUS=$6
             MAX_VUS=$7
             QPS_LIST=$8
             OUTDIR=$9
                        
             #echo "SCRIPT: $SCRIPT"
             echo "URL: $URL"
             echo "PAYLOAD: $PAYLOAD"
             echo "HEADERS: $HEADERS"
             echo "DURATION: $DURATION"
             echo "VUS: $VUS"
             echo "MAX_VUS: $MAX_VUS"
             echo "QPS_LIST: $QPS_LIST"
             echo "OUTDIR: $OUTDIR"
                        
             if [ -n "$OUTDIR" ]; then
                 mkdir -p "$OUTDIR"
             fi
                        
             # 判断是否指定了 OUTDIR
             if [ -n "$OUTDIR" ]; then
                 OUTFILE="${OUTDIR}/qps_${QPS}.json"
                 SUMMARY_ARG="--summary-export=$OUTFILE"
             else
                 SUMMARY_ARG=""
             fi
                        
             echo "开始压测所有 QPS 配置..."
                        
             for QPS in ${QPS_LIST[@]}; do
                 echo "--------------------------------------"
                 echo "开始压测：QPS = $QPS"
                        
                 k6 run "$SCRIPT" \\
                     --env TARGET_URL="$URL" \\
                     --env PAYLOAD="$PAYLOAD" \\
                     --env HEADERS="$HEADERS" \\
                     --env QPS="$QPS" \\
                     --env DURATION="$DURATION" \\
                     --env VUS="$VUS" \\
                     --env MAX_VUS="$MAX_VUS" \\
                     $SUMMARY_ARG
                 echo
             done
                        
             echo "执行结束!"   
            """;
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
    public static class TestOption {
        /**
         * 请求类型
         * 1 http post 接口
         * 2 http get 接口
         * 3 websocket 接口
         */
        private Integer type;

        /**
         * 请求的url
         * eg http post：http://127.0.0.1:10030/action/v1.0/SendQryOne/Order
         * eg http get: http://127.0.0.1:10030/query/v1.0/CanceledOrder?MemberID=004a05ee-d7e6-44bb-b238-3feb205de1d0
         * eg websocket: ws://127.0.0.1:11000
         */
        private String url;

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
        private List<Integer> qpsList;

        /**
         * 输出报告目录
         * 后续接grafana的话可能需要用到
         * 为空的话，报告输出到控制台，内容由testAPI函数以string类型返回
         */
        private String outDir;
    }
}
