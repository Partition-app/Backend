import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const locationDuration = new Trend('location_share_duration');
const reportDuration = new Trend('report_duration');
const autoAssignDuration = new Trend('auto_assign_duration');

const BASE_URL = __ENV.BASE_URL || 'https://api.partition.site';
const TOKEN = __ENV.TOKEN;
const TODAY = new Date().toISOString().split('T')[0];

const headers = {
  Authorization: `Bearer ${TOKEN}`,
  'Content-Type': 'application/json',
};

export const options = {
  scenarios: {
    // 시나리오 1: 위치 공유 API (30초 폴링 패턴 시뮬레이션)
    location_share: {
      executor: 'ramping-vus',
      startTime: '0s',
      stages: [
        { duration: '1m', target: 10 },
        { duration: '2m', target: 30 },
        { duration: '2m', target: 50 },
        { duration: '1m', target: 0 },
      ],
      exec: 'locationShareFlow',
      tags: { scenario: 'location_share' },
    },

    // 시나리오 2: 리포트 조회 API
    report: {
      executor: 'ramping-vus',
      startTime: '6m',
      stages: [
        { duration: '1m', target: 10 },
        { duration: '2m', target: 30 },
        { duration: '2m', target: 50 },
        { duration: '1m', target: 0 },
      ],
      exec: 'reportFlow',
      tags: { scenario: 'report' },
    },

    // 시나리오 3: 집안일 자동배정 API (Spring → FastAPI)
    auto_assign: {
      executor: 'ramping-vus',
      startTime: '12m',
      stages: [
        { duration: '1m', target: 5 },
        { duration: '2m', target: 15 },
        { duration: '2m', target: 30 },
        { duration: '1m', target: 0 },
      ],
      exec: 'autoAssignFlow',
      tags: { scenario: 'auto_assign' },
    },

    // 시나리오 4: 혼합 부하 (실제 사용 패턴)
    mixed: {
      executor: 'ramping-vus',
      startTime: '18m',
      stages: [
        { duration: '1m', target: 20 },
        { duration: '3m', target: 50 },
        { duration: '1m', target: 0 },
      ],
      exec: 'mixedFlow',
      tags: { scenario: 'mixed' },
    },
  },

  thresholds: {
    'http_req_duration{scenario:location_share}': ['p(95)<1000'],
    'http_req_duration{scenario:report}': ['p(95)<2000'],
    'http_req_duration{scenario:auto_assign}': ['p(95)<5000'],
    errors: ['rate<0.05'],
  },
};

// 시나리오 1: 위치 공유
export function locationShareFlow() {
  const res = http.get(
    `${BASE_URL}/api/households/location-events/near-home`,
    { headers }
  );
  check(res, { '위치공유 200': (r) => r.status === 200 });
  errorRate.add(res.status !== 200);
  locationDuration.add(res.timings.duration);
  sleep(1);
}

// 시나리오 2: 리포트 조회
export function reportFlow() {
  const res = http.get(
    `${BASE_URL}/api/reports`,
    { headers }
  );
  check(res, { '리포트 200': (r) => r.status === 200 });
  errorRate.add(res.status !== 200);
  reportDuration.add(res.timings.duration);
  sleep(1);
}

// 시나리오 3: 집안일 자동배정
export function autoAssignFlow() {
  const endDate = new Date(Date.now() + 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  const payload = JSON.stringify({
    startDate: TODAY,
    endDate: endDate,
  });
  const res = http.post(
    `${BASE_URL}/api/chores/auto-assign`,
    payload,
    { headers }
  );
  check(res, { '자동배정 200': (r) => r.status === 200 });
  errorRate.add(res.status !== 200);
  autoAssignDuration.add(res.timings.duration);
  sleep(3);
}

// 시나리오 4: 혼합 (70% 위치공유 / 20% 리포트 / 10% 자동배정)
export function mixedFlow() {
  const roll = Math.random();

  if (roll < 0.7) {
    const res = http.get(
      `${BASE_URL}/api/households/location-events/near-home`,
      { headers }
    );
    check(res, { '혼합-위치공유 200': (r) => r.status === 200 });
    errorRate.add(res.status !== 200);
  } else if (roll < 0.9) {
    const res = http.get(`${BASE_URL}/api/reports`, { headers });
    check(res, { '혼합-리포트 200': (r) => r.status === 200 });
    errorRate.add(res.status !== 200);
  } else {
    const endDate = new Date(Date.now() + 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    const payload = JSON.stringify({ startDate: TODAY, endDate: endDate });
    const res = http.post(
      `${BASE_URL}/api/chores/auto-assign`,
      payload,
      { headers }
    );
    check(res, { '혼합-자동배정 200': (r) => r.status === 200 });
    errorRate.add(res.status !== 200);
  }

  sleep(1);
}