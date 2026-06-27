package com.notification.lab.backend.domain.fanout

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class EventRequest(
    val recipientIds: List<String>,
    val payload: String,
)

/**
 * POST /events — 이벤트 1건을 수신자들에게 팬아웃한다.
 *
 * M0: fanout이 동기라 이 응답 시간 ≈ 팬아웃 완료 시간이다. (그래서 측정 대상.)
 *     http_server_requests{uri=/events} 의 p95/p99를 본다.
 * M1: 큐에 넣고 즉시 202를 반환하도록 바뀐다 — 그때 응답 시간이 꺾이는 걸 측정한다.
 *
 * (NOTE: 의도적으로 인증/검증을 최소화했다. recipientIds를 요청에서 그대로 받는 건
 *  M0 측정 편의다. 실서비스라면 debt-reviewer R1[신원]·R5[입력검증]에 걸린다.)
 */
@RestController
class EventController(
    private val fanoutService: FanoutService,
) {
    @PostMapping("/events")
    fun publish(@RequestBody request: EventRequest): ResponseEntity<Void> {
        fanoutService.fanout(request.recipientIds, request.payload)
        return ResponseEntity.ok().build()
    }
}
