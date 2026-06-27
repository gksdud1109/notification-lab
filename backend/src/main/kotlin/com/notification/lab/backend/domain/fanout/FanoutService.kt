package com.notification.lab.backend.domain.fanout

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage

/**
 * 팬아웃 (M0 = 일부러 동기).
 *
 * ★ 이 클래스가 M0 학습의 심장이다 ★
 * fanout()은 호출한 요청(Tomcat) 스레드에서 모든 수신자에게 순차로 sendMessage 한다.
 *  - 수신자가 많을수록 POST /events 응답이 길어진다.
 *  - 느리거나 죽은 클라이언트 1명의 sendMessage가 블로킹되면 그 요청 스레드가 잡힌다.
 *  - 동접 이벤트가 늘면 Tomcat 스레드풀(application.yaml의 max)이 소진되고
 *    신규 요청이 503/타임아웃 난다.
 *
 * M1에서 할 일: 이 fanout() 호출을 큐 뒤의 worker로 떼어내, POST /events는
 * "큐에 넣고 즉시 202"만 하게 만든다. 그러면 응답 시간이 꺾이는 걸 측정한다.
 * → 이 클래스가 M1의 이음매(seam)다. 시그니처를 함부로 바꾸지 마라.
 */
@Service
class FanoutService(
    private val registry: SessionRegistry,
    meter: MeterRegistry,
) {
    // 측정: 동기 팬아웃 1건 처리 시간. recipient 수와의 상관을 본다.
    private val fanoutTimer: Timer = Timer.builder("notiflab.fanout.duration")
        .description("synchronous fan-out of one event")
        .publishPercentiles(0.5, 0.95, 0.99)
        .register(meter)

    fun fanout(recipientIds: List<String>, payload: String) {
        fanoutTimer.recordCallable {
            val message = TextMessage(payload)
            for (userId in recipientIds) {
                for (session in registry.sessionsOf(userId)) {
                    // 블로킹 send — M0 병목의 근원. 느린 소비자가 여기서 요청 스레드를 잡는다.
                    if (session.isOpen) session.sendMessage(message)
                }
            }
        }
    }
}
