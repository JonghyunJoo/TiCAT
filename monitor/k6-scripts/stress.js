import http from 'k6/http';
import { sleep, check, fail } from 'k6';

const BASE_URL = 'http://172.25.0.1:8000/queue-service/';
const scheduleId = 1;

// export let options = {
//     vus: 1, // 1 user looping for 1 minute
//     duration: '10s',
//     thresholds: {
//         http_req_duration: ['p(99)<800'], // 요청의 99%가 800ms 미만으로 완료되어야 함
//     },
// };
//
// export default function () {
//     // 대기열 요청
//     const waitingRes = http.post(`${BASE_URL}`,JSON.stringify({
//         userId: 1,
//         concertScheduleId: scheduleId
//     }), {
//         headers: { 'Content-Type': 'application/json' },
//     });
//     check(waitingRes, {
//         'waiting check': res => res.status === 200
//     });
//     if (waitingRes.status !== 200) {
//         fail('Failed check for waiting-page');
//     }
//
//     return waitingRes;
// }

export const options = {
    stages: [
        { duration: '5s', target: 200 },
        { duration: '5s', target: 400 },
        { duration: '5s', target: 600 },
        { duration: '5s', target: 800 },
        { duration: '5s', target: 1000 },
        { duration: '10s', target: 1000 },
        // { duration: '10s', target: 1000 },
        // { duration: '10s', target: 1000 },
        { duration: '5s', target: 0 }
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% 요청이 500ms 이하
    },
};

export default function () {
    let userId = __VU;

    // 1. 대기열 추가 요청 (POST)
    let queueRes = http.post(`${BASE_URL}`, JSON.stringify({
        userId: userId,
        concertScheduleId: scheduleId
    }), {
        headers: { 'Content-Type': 'application/json' }
    });

    check(queueRes, {
        '대기열 응답 코드 200': (r) => r.status === 200,
    });

    if (queueRes.status !== 200) {
        console.error(`Failed queue request: ${queueRes.status} - ${queueRes.body}`);
        fail('Failed check for waiting-page');
    }

    sleep(1);

// 2. 대기열 상태 확인 (GET)
    let statusRes = http.get(`${BASE_URL}?userId=${userId}&concertScheduleId=${scheduleId}`, {
        headers: { 'Content-Type': 'application/json' }
    });

    check(statusRes, {
        '대기열 상태 응답 코드 200': (r) => r.status === 200,
    });

    if (statusRes.status !== 200) {
        console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
        fail('Failed check for queue status');
    }

    sleep(1);
}
