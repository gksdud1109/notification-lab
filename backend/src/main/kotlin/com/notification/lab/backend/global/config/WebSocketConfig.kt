package com.notification.lab.backend.global.config

import com.notification.lab.backend.domain.fanout.NotificationWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * WebSocket 엔드포인트 등록 (M0).
 * 클라이언트는 ws://host:8080/ws?userId=<id> 로 연결한다.
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val handler: NotificationWebSocketHandler,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler, "/ws").setAllowedOrigins("*")
    }
}
