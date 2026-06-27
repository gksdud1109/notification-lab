package com.notification.lab.backend.domain.notification.controller

/*
 * NotificationController — M1 예정.
 *
 * M0(현재)는 저장 없이 인메모리 팬아웃만 측정한다 → 실제 엔드포인트는
 * com.notification.lab.backend.domain.fanout.EventController (POST /events) 가 담당한다.
 *
 * M1에서 이 컨트롤러로 "알림 저장 + 조회(읽음/히스토리)"를 붙이고,
 * Notification(@Entity)·NotificationService 를 구현한다. 그때
 * BackendApplication 의 DataSource/JPA autoconfig exclude 를 제거하고
 * application.yaml 의 datasource 블록을 되살린다.
 *
 * (직전 작업에서 멈춰 있던 미완성 컨트롤러는 컴파일을 막아 M0 실행을 방해하므로
 *  이 placeholder 로 대체했다. 원본은 git first commit 에 남아 있다.)
 */
