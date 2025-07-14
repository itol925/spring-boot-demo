import ws from 'k6/ws';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

export const options = {
    scenarios: {
        websocket_load: {
            executor: 'per-vu-iterations',
            vus: parseInt(__ENV.VUS || '1'),
            iterations: 1,
            maxDuration: __ENV.DURATION + 's' || '10s',
        }
    }
};

const ADDR = __ENV.ADDR;
const VUS = parseInt(__ENV.VUS || '1');
const QPS = parseInt(__ENV.QPS || '1');
const REQUESTS = JSON.parse(__ENV.REQUESTS);
const totalRatio = REQUESTS[REQUESTS.length - 1].ratio;

// 统计延迟的趋势对象
const latencyTrend = new Trend('ws_latency', true);
const errLogMap = new Map();

export default function () {
    const connectionQPS = QPS / VUS;
    const intervalSeconds = 1 / connectionQPS;

    const res = ws.connect(ADDR, {}, function (socket) {
        const sendTimes = new Map();
        let seq = 0;

        socket.on('open', () => {
            console.log(`VU ${__VU} 已连接, intervalSeconds=` + intervalSeconds);
            // 设置定时发送消息
            socket.setInterval(() => {
                const request = getRequest(seq);
                const requestNo = seq++;
                const payload = injectRequestNo(request.payload, requestNo);

                sendTimes.set(requestNo, Date.now());
                socket.send(payload);
                console.log("send << " + requestNo + ", action:" + payload);
            }, intervalSeconds * 1000); // 发送间隔（秒）

            socket.setTimeout(() => {
                socket.close();
                console.log(`VU ${__VU} 时间到。关闭连接`);
            }, __ENV.DURATION * 1000);
        });

        // 接收响应并记录延迟
        socket.on('message', (data) => {
            try {
                const msg = JSON.parse(data);
                check(msg, { 'errorCode = 0': (m) => m && m.errorCode === 0 });

                // catch error log
                if (msg && msg.errorCode !== 0) {
                    if (!errLogMap.has(msg.errorCode)) {
                        errLogMap.set(msg.errorCode, msg);
                        console.log('<<<< Error:errorCode=' + msg.errorCode + ", data=" + JSON.stringify(msg))
                    }
                }

                const reqNo = msg.requestNo;
                // console.log("recv >> " + reqNo);
                const sendTime = sendTimes.get(reqNo);
                if (sendTime) {
                    const latency = Date.now() - sendTime;
                    latencyTrend.add(latency);
                    sendTimes.delete(reqNo);
                } else {
                    console.warn(`未找到 RequestNo: ${reqNo}`);
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
        }, 5000);
    });

    check(res, { 'status = 101': (r) => r && r.status === 101 });
}
// 将 payload 插入 RequestNo 字段
function injectRequestNo(payload, requestNo) {
    payload.RequestNo = requestNo;
    return JSON.stringify(payload);
}

function getRequest(n) {
    let r = n % totalRatio;
    for (const request of REQUESTS) {
        if (r < request.ratio) {
            return request;
        }
    }
    return null;
}