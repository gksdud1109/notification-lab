package com.notification.lab.backend.domain.fanout

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

/**
 * WebSocket 핸들러 (M0).
 *
 * 연결 시 `?userId=` 쿼리파람으로 사용자를 식별해 레지스트리에 등록한다.
 * (M0는 인증 없음 — 일부러. 인증은 측정 변수와 무관하니 나중/실서비스의 일이다.)
 *
 * 클라이언트가 보내는 메시지는 처리하지 않는다. 이 랩은 "서버→클라 팬아웃"만 측정한다.
 */
@Component
class NotificationWebSocketHandler(
    private val registry: SessionRegistry,
) : TextWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = userIdOf(session)
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA)
            return
        }
        registry.register(userId, session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        userIdOf(session)?.let { registry.unregister(it, session) }
    }

    private fun userIdOf(session: WebSocketSession): String? =
        session.uri?.query
            ?.split("&")
            ?.firstOrNull { it.startsWith("userId=") }
            ?.substringAfter("userId=")
            ?.takeIf { it.isNotBlank() }
}
