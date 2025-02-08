import http from 'k6/http';
import {sleep, check, fail, group} from 'k6';

const BASE_URL = 'http://apigateway-service:8000';
const tps_A = 200;
const tps_B = 1000;
export const options = {
    scenarios: {
        // load_test: {
        //     executor: 'ramping-vus',
        //     startVUs: 0,
        //     stages: [
        //         {duration: '5s', target: tps_A},
        //         {duration: '5s', target: tps_A * 2},
        //         {duration: '5s', target: tps_A * 3},
        //         {duration: '5s', target: tps_A * 4},
        //         {duration: '5s', target: tps_A * 5},
        //         {duration: '10s', target: tps_A * 5},
        //         {duration: '10s', target: tps_A * 5},
        //         {duration: '10s', target: tps_A * 5},
        //         {duration: '5s', target: 0}
        //     ],
        // },
        peak_test: {
            executor: 'ramping-arrival-rate',
            startRate: 10,
            timeUnit: '1s',
            preAllocatedVUs: 200,
            maxVUs: 2000,
            stages: [
                { duration: '10s', target: tps_B },
                { duration: '20s', target: tps_B*2 },
                { duration: '30s', target: tps_B },
                { duration: '10s', target: tps_B*4 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(99)<500'],
    },
};

// export default function () {
//     const TEST_USER_IDS = [1, 2, 3, 4, 5];
//     const userId = TEST_USER_IDS[Math.floor(Math.random() * TEST_USER_IDS.length)];
//
//     const rechargePayload = JSON.stringify({
//         userId: userId,
//         amount: 1000,
//     });
//
//     const waitingRes = http.get(`${BASE_URL}/wallet-service/`, rechargePayload, {
//         headers: { 'Content-Type': 'application/json' },
//     });
//
//     console.log(`UserID: ${userId}, Response status: ${waitingRes.status}`);
//
//     check(waitingRes, {
//         'waiting check': res => res.status === 200
//     });
//
//     if (waitingRes.status !== 200) {
//         console.error(`Request failed for user ${userId} - Status: ${waitingRes.status}, Body: ${waitingRes.body}`);
//         fail('Failed check for waiting-page');
//     }
//
//     sleep(1);
// }
export default function () {
    // const TEST_USER_IDS = [1, 2, 3, 4, 5];
    // const userId = TEST_USER_IDS[Math.floor(Math.random() * TEST_USER_IDS.length)];
    //
    //
    // const waitingRes = http.get(`${BASE_URL}/wallet-service/${userId}`);
    //
    // console.log(`UserID: ${userId}, Response status: ${waitingRes.status}`);
    //
    // check(waitingRes, {
    //     'waiting check': res => res.status === 200
    // });
    //
    // if (waitingRes.status !== 200) {
    //     console.error(`Request failed for user ${userId} - Status: ${waitingRes.status}, Body: ${waitingRes.body}`);
    //     fail('Failed check for waiting-page');
    // }
    //
    // sleep(1);

//     let userId = __VU;
//     let scheduleId = 1;
//
//     // 1. 대기열 추가 요청 (POST)
//     group('Step 1: create queue', function () {
//     let queueRes = http.post(`${BASE_URL}/queue-service/`, JSON.stringify({
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
//     });
//     sleep(1);
//
// // 2. 대기열 상태 확인 (GET)
//     group('Step 2: get queue status', function () {
//     let statusRes = http.get(`${BASE_URL}/queue-service/?userId=${userId}&concertScheduleId=${scheduleId}`, {
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
//     });
//     sleep(1);


    let userId = __VU;
    let concertId = 1;
    let concertScheduleId = 1;
    const TEST_SEAT_IDS = [1, 2, 3, 4, 5, 6];
    const seatId = TEST_SEAT_IDS[Math.floor(Math.random() * TEST_SEAT_IDS.length)];

    group('Step 1: 콘서트 조회', function () {
        let queueRes = http.get(`${BASE_URL}/concert-service/?page=0&size=10&orderBy=concertStartDate&orderDirection=ASC`, {
            headers: {'Content-Type': 'application/json'}
        });
        check(queueRes, {
            '대기열 응답 코드 200': (r) => r.status === 200,
        });

        if (queueRes.status !== 200) {
            console.error(`Failed queue request: ${queueRes.status} - ${queueRes.body}`);
            fail('Failed check for waiting-page');
        }
    });
    sleep(1);


    group('Step 2: get schedule from concert', function () {
        let statusRes = http.get(`${BASE_URL}/concert-service/concertSchedule/${concertId}`, {
            headers: {'Content-Type': 'application/json'}
        });

        check(statusRes, {
            '대기열 상태 응답 코드 200': (r) => r.status === 200,
        });

        if (statusRes.status !== 200) {
            console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
            fail('Failed check for queue status');
        }
    });

    sleep(1);

    group('Step 3: get seat chart', function () {
        let statusRes = http.get(`${BASE_URL}/seat-service/seats/${concertScheduleId}`, {
            headers: {'Content-Type': 'application/json'}
        });

        check(statusRes, {
            '대기열 상태 응답 코드 200': (r) => r.status === 200,
        });

        if (statusRes.status !== 200) {
            console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
            fail('Failed check for queue status');
        }
    });

    sleep(1);

    group('Step 4: select seat', function () {
        let statusRes = http.put(`${BASE_URL}/seat-service/selectSeat`, JSON.stringify({
            userId: userId,
            seatId: seatId
        }), {
            headers: {'Content-Type': 'application/json'}
        });

        check(statusRes, {
            '대기열 상태 응답 코드 200, 400, 409': (r) => r.status === 200 || r.status === 400 || r.status === 409,
        });

        if (statusRes.status !== 200 && statusRes.status !== 400 && statusRes.status !== 409) {
            console.error(`Failed status check: ${statusRes.status} - ${statusRes.body}`);
            fail('Failed check for queue status');
        }
    });

    sleep(1);

    group('Step 5: create reservation', function () {
        let queueRes = http.post(`${BASE_URL}/reservation-service/`, JSON.stringify({
            seatIdList: [1],
            userId: userId
        }), {
            headers: {'Content-Type': 'application/json'}
        });

        check(queueRes, {
            '대기열 응답 코드 200': (r) => r.status === 200,
        });

        if (queueRes.status !== 200) {
            console.error(`Failed queue request: ${queueRes.status} - ${queueRes.body}`);
            fail('Failed check for waiting-page');
        }
    });
    sleep(1);

}
