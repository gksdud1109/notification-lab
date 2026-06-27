# notification-lab

부하로 병목을 깨뜨리고, worker/DB/Kafka/Redis/batch 등등의 궁금했던 기술들을 **필요한 순간에만** 도입하는
실시간 알림 시스템 실험 레포입니다. 제품이 아니라 *측정 가능한 분산 시스템 실험*입니다.

## 규칙

- UI / 로그인·회원가입 / 모바일 푸시 **금지**
- 예쁜 기능보다 **병목 측정 우선**
- 한 마일스톤 = **한 변수만** 바꾼다 (변인 통제)
- 매 마일스톤 **before/after 리포트 필수**
- 부하 생성기(k6)와 측정 대상(app)의 **CPU 분리** (안 그러면 숫자가 거짓)

## 로드맵 (각 단계 = 측정하는 단일 변수)

| M | 추가하는 단 하나 | 측정 질문 |
|---|---|---|
| **M0** | 인메모리 동기 팬아웃 | 동기 팬아웃은 *언제* 무너질까 (= before 베이스라인) |
| M1 | + DB 저장 | fan-out write 증폭(1 event→N insert)이 응답을 얼마나 먹을까 |
| M2 | + worker 비동기 분리 | 팬아웃·DB write를 요청 경로에서 떼면 p99가 얼마나 꺾일까 |
| M3 | + Kafka (durable) | 유실 방지 비용 / 처리량 |
| M4 | + 분산 세션 (다중 서버) | 다중 서버 팬아웃 라우팅 |
| M5 | + batch (오프라인 pending) | 멱등 · 재시도 |

## 실행

```bash
# 앱 + Prometheus + Grafana 기동
docker compose up -d --build

# 부하 (별도 CPU로 격리 실행)
docker compose --profile load run --rm k6 run /scripts/k6-m0.js

# 관측
#  app metrics : http://localhost:8080/actuator/prometheus
#  Prometheus  : http://localhost:9090
#  Grafana     : http://localhost:3000
```

로컬에서 앱만:
```bash
cd backend && ./gradlew bootRun
```

## API (M0)

- `ws://localhost:8080/ws?userId=<id>` — WebSocket 연결(수신자 등록)
- `POST /events  { "recipientIds": ["u1","u2"], "payload": "..." }` — 동기 팬아웃

## 측정 지표 (Prometheus)

- `http_server_requests_seconds{uri="/events"}` — POST 응답 p50/95/99
- `notiflab_fanout_duration_seconds` — 동기 팬아웃 1건 처리 시간
- `notiflab_sessions_active` — 활성 WS 세션 수
- `tomcat_threads_busy_threads` — 스레드풀 소진 관전 포인트

---

## ✍️ M0 리포트 (학습본체~ 채울예정~)

### 가설
> 동기 팬아웃은 recipient 수 × 동접에 비례해 POST /events 응답을 끈다
> (a) 순차 sendMessage가 Tomcat 스레드를 점유 → (b) recipient↑ 또는 느린 클라 1명
> → (c) 스레드풀(max 200) 소진 → (d) 신규 요청 503/타임아웃
> **어느 단계가 먼저일까?** ← 측정으로 답하기

### SLO (고정 기준선 — 한 번 정하면 M4까지 불변)
- p99 < ____ ms
- error rate < ____ %
- 동접 ____, event rate ____ rps, recipient/event ____

### 부하 조건
- connected users / event rate / recipients per event / duration:
- 격리 확인 (app cpus vs k6 cpus, 호스트 총 코어):

### 결과
- p50 / p95 / p99:
- error rate:
- tomcat busy threads (피크):
- notiflab_fanout_duration p99:
- **먼저 무너진 축** (동접 vs recipient vs rate):

### 가설 vs 실측
- 

### 다음 단계 — M1 필요성을 숫자로
-
