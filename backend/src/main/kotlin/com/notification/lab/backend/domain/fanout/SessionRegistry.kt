package com.notification.lab.backend.domain.fanout

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 인메모리 세션 레지스트리 (M0).
 *
 * 한 userId가 다중 연결(모바일+웹)을 가질 수 있어 값이 리스트다.
 *
 * M0의 한계 = 학습 포인트:
 * 이 맵은 이 서버 프로세스 안에만 산다. M4에서 서버를 여러 대로 늘리면
 * "수신자가 어느 서버에 붙었는지" 알 수 없게 되고, 그때 이 레지스트리를
 * 분산(Redis pub/sub 등)으로 바꾼다. 지금은 단일 프로세스라 충분하다.
 */
@Component
class SessionRegistry(meter: MeterRegistry) {

    private val sessions = ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>>()

    init {
        // 측정: 현재 활성 WebSocket 세션 수. k6로 동접을 올릴 때 이 값이 따라 오르는지 확인.
        meter.gauge("notiflab.sessions.active", sessions) { map ->
            map.values.sumOf { it.size }.toDouble()
        }
    }

    fun register(userId: String, session: WebSocketSession) {
        sessions.computeIfAbsent(userId) { CopyOnWriteArrayList() }.add(session)
    }

    fun unregister(userId: String, session: WebSocketSession) {
        sessions[userId]?.let { list ->
            list.remove(session)
            if (list.isEmpty()) sessions.remove(userId)
        }
    }

    fun sessionsOf(userId: String): List<WebSocketSession> = sessions[userId] ?: emptyList()
}
