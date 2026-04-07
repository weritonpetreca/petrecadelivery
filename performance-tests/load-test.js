import http from 'k6/http'
import { check, sleep } from 'k6'

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],

    threshold: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500']
    },
};

export function setup() {
    console.log('🔑 Requesting JWT from Keycloak...');
    const url = 'http://localhost:8082/realms/petreca-realm/protocol/openid-connect/token';
    const payload = {
        client_id: 'petreca-api-client',
        grant_type: 'password',
        username: __ENV.TEST_USER_NAME,
        password: __ENV.TEST_USER_PASSWORD
    };

    const res = http.post(url, payload);

    check(res, {
        'Keycloak auth successful': (r) => r.status === 200
    });

    return { token: res.json('access_token') };
}

export default function loadTest (data) {
    const url = 'http://localhost:9999/api/v1/deliveries?page=0&size=20&sort=id';

    const params = {
        headers: {
            'Authorization': `Bearer ${data.token}`,
            'Content-Type': 'application/json'
        }
    };

    const res = http.get(url, params);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'transaction is fast': (r) => r.timings.duration < 200
    });

    sleep(1);
}

