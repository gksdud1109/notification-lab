import http from 'k6/http';
import ws from 'k6/ws';
import { check } from 'k6';
import { Rate } from 'k6/metrics';

// ─────────────────────────────────────────────────────────────────────
// M0 부하 시나리오 — 두 축(동접 WS, event rate)을 따로 올려
// "어느 축이 먼저 무너뜨리나"를 본다.
//
// ★ 네가 직접 정할 것 (지금은 placeholder) ★
//   - 아래 숫자가 곧 M0~M4 전체의 고정 기준선이다. 한 번 정하면 바꾸지 마라.
//   - README의 가설/SLO와 이 숫자를 맞춰라.
// ─────────────────────────────────────────────────────────────────────

const TARGET = __ENV.TARGET || 'http://localhost:8080';
const WS_URL = TARGET.replace('http', 'ws') + '/ws';

const CONNECTED  = Number(__ENV.CONNECTED  || 1000); // 유지할 동접 WS 수
const EVENT_RPS  = Number(__ENV.EVENT_RPS  || 50);   // 초당 이벤트
const RECIPIENTS = Number(__ENV.RECIPIENTS || 100);  // 이벤트당 수신자 수

export const options = {
  scenarios: {
    // 1) 동접 WS 연결을 ramp-up 후 유지
    hold_connections: {
      executor: 'ramping-vus',
      exec: 'holdConnection',
      startVUs: 0,
      stages: [
        { duration: '30s', target: CONNECTED },
        { duration: '3m',  target: CONNECTED },
      ],
    },
    // 2) 그 위에 이벤트를 일정 속도로 쏜다
    post_events: {
      executor: 'constant-arrival-rate',
      exec: 'postEvent',
      rate: EVENT_RPS,
      timeUnit: '1s',
      duration: '3m',
      startTime: '30s',
      preAllocatedVUs: 50,
      maxVUs: 300,
    },
  },
  thresholds: {
    // ← 네 SLO로 바꿔라. 이건 기준선 placeholder 일 뿐.
    'http_req_duration{scenario:post_events}': ['p(95)<500', 'p(99)<1000'],
    'http_req_failed{scenario:post_events}': ['rate<0.01'],
  },
};

const wsErrors = new Rate('ws_connect_errors');

export function holdConnection() {
  const userId = `u${__VU}`;
  const res = ws.connect(`${WS_URL}?userId=${userId}`, {}, (socket) => {
    // 연결만 유지하고 3분 뒤 닫는다. (이 랩은 서버→클라 팬아웃만 측정)
    socket.on('open', () => socket.setTimeout(() => socket.close(), 180000));
    socket.on('error', () => wsErrors.add(1));
  });
  check(res, { 'ws upgrade 101': (r) => r && r.status === 101 });
}

export function postEvent() {
  // 수신자를 동접 풀에서 무작위로 RECIPIENTS명 뽑는다.
  const ids = Array.from(
    { length: RECIPIENTS },
    () => `u${1 + Math.floor(Math.random() * CONNECTED)}`,
  );
  const res = http.post(
    `${TARGET}/events`,
    JSON.stringify({ recipientIds: ids, payload: 'ping' }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(res, { 'event 200': (r) => r.status === 200 });
}
