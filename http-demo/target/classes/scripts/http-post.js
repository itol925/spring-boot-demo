import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const ADDR = __ENV.ADDR;
const VUS = parseInt(__ENV.VUS || '1');
const QPS = parseInt(__ENV.QPS || '1');
const DURATION = (__ENV.DURATION + 's') || '10s';
const MAX_VUS = Number(__ENV.MAX_VUS || '200');

const REQUESTS = JSON.parse(__ENV.REQUESTS);
const totalRatio = REQUESTS[REQUESTS.length - 1].ratio;


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
const errLogMap = new Map();
export default function () {
    const request = getRequest(__ITER);
    const url = ADDR + request.path;
    const res = http.post(url, JSON.stringify(request.payload), request.header);
    const msg = JSON.parse(res.body);
    // console.log("---- recv:" + msg.result[0].table + "." + __ITER)
    check(msg, { 'errorCode = 0': (m) => m && m.errorCode === 0 });
    // catch error log
    if (msg && msg.errorCode !== 0) {
        if (!errLogMap.has(msg.errorCode)) {
            errLogMap.set(msg.errorCode, msg);
            console.log('<<<< Error:errorCode=' + msg.errorCode + ", data=" + JSON.stringify(msg))
        }
    }

    latency.add(res.timings.duration);
    check(res, {
        'status = 200': (r) => r.status === 200,
    });
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