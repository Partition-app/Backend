import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const errorRate = new Rate('errors');
const nearHomeDuration = new Trend('near_home_duration');
const alarmDuration = new Trend('alarm_duration');
const calendarDuration = new Trend('calendar_duration');

export const options = {
  stages: [
    { duration: '1m', target: 10 },
    { duration: '2m', target: 30 },
    { duration: '2m', target: 50 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'],  // 95% 요청이 1초 이내
    errors: ['rate<0.05'],              // 에러율 5% 미만
  },
};

const BASE_URL = __ENV.BASE_URL || 'https://api.partition.site';
const TOKEN = __ENV.TOKEN;

const headers = {
  Authorization: `Bearer ${TOKEN}`,
  'Content-Type': 'application/json',
};

export default function () {
  const today = new Date().toISOString().split('T')[0];

  // 1. 귀가 공유 상태 조회 (30초마다 폴링되는 핵심 API)
  const nearHomeRes = http.get(
    `${BASE_URL}/api/households/location-events/near-home`,
    { headers }
  );
  check(nearHomeRes, { '귀가공유 200': (r) => r.status === 200 });
  errorRate.add(nearHomeRes.status !== 200);
  nearHomeDuration.add(nearHomeRes.timings.duration);

  sleep(1);

  // 2. 알림 목록 조회 (full scan + filesort 확인된 API)
  const alarmsRes = http.get(`${BASE_URL}/api/alarms`, { headers });
  check(alarmsRes, { '알림목록 200': (r) => r.status === 200 });
  errorRate.add(alarmsRes.status !== 200);
  alarmDuration.add(alarmsRes.timings.duration);

  sleep(1);

  // 3. 일간 캘린더 조회
  const dailyRes = http.get(
    `${BASE_URL}/api/calendars/daily?date=${today}`,
    { headers }
  );
  check(dailyRes, { '일간캘린더 200': (r) => r.status === 200 });
  errorRate.add(dailyRes.status !== 200);
  calendarDuration.add(dailyRes.timings.duration);

  sleep(1);
}