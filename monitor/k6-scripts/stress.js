import http from 'k6/http';
import { sleep, check, fail } from 'k6';

const BASE_URL = 'http://172.25.0.1:8000';
const rampup_duration = "5s"
const steady_duration = "10s"
const tps_A = 50;
const tps_B = 100;
export const options = {
    scenarios: {
        load_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: rampup_duration, target: tps_A },
                { duration: steady_duration, target: tps_A },
                { duration: rampup_duration, target: tps_A * 2},
                { duration: steady_duration, target: tps_A * 2},
                { duration: rampup_duration, target: tps_A * 3},
                { duration: steady_duration, target: tps_A * 3},
            ],
        },
        peak_test: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            preAllocatedVUs: 200,
            maxVUs: 1000,
            stages: [
                { duration: rampup_duration, target: tps_B },
                { duration: steady_duration, target: tps_B },
                { duration: rampup_duration, target: tps_B * 2},
                { duration: steady_duration, target: tps_B * 2},
                { duration: rampup_duration, target: tps_B * 3},
                { duration: steady_duration, target: tps_B * 3},
            ],
        },
    },
};

export default function () {
    const TEST_USER_IDS = [1, 2, 3, 4, 5];
    const userId = TEST_USER_IDS[Math.floor(Math.random() * TEST_USER_IDS.length)];

    const rechargePayload = JSON.stringify({
        userId: userId,
        amount: 1000,
    });

    const waitingRes = http.put(`${BASE_URL}/wallet-service/`, rechargePayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    console.log(`UserID: ${userId}, Response status: ${waitingRes.status}`);

    check(waitingRes, {
        'waiting check': res => res.status === 200
    });

    if (waitingRes.status !== 200) {
        console.error(`Request failed for user ${userId} - Status: ${waitingRes.status}, Body: ${waitingRes.body}`);
        fail('Failed check for waiting-page');
    }

    sleep(1);
}


// export default function () {
//     let userId = __VU;
//
//     // 1. 대기열 추가 요청 (POST)
//     let queueRes = http.post(`${BASE_URL}`, JSON.stringify({
//         userId: userId,
//         concertScheduleId: scheduleId
//     }), {
//         headers: { 'Content-Type': 'application/json' }
//     });
//
//     check(queueRes, {
//         '대기열 응답 코드 200': (r) => r.status === 200,
//     });
//
//     if (queueRes.status !== 200) {
//         console.error(`Failed queue request: ${queueRes.status} - ${queueRes.body}`);
//         fail('Failed check for waiting-page');
//     }
//
//     sleep(1);
//
// // 2. 대기열 상태 확인 (GET)
//     let statusRes = http.get(`${BASE_URL}?userId=${userId}&concertScheduleId=${scheduleId}`, {
//         headers: { 'Content-Type': 'application/json' }
//     });
//
//     check(statusRes, {
//         '대기열 상태 응답 코드 200': (r) => r.status === 200,
//     });
//
//     if (statusRes.status !== 200) {
//         console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
//         fail('Failed check for queue status');
//     }
//
//     sleep(1);
// }
