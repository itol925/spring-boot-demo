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
