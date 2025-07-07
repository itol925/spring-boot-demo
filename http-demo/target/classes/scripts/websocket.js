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