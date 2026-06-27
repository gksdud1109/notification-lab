package com.notification.lab.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

// M0는 인메모리 측정 전용이다. DB/JPA는 M1(DB 저장 + fan-out write 증폭 측정)에서 켠다.
// 그때 아래 exclude를 지우고 application.yaml의 datasource를 되살리면 된다.
// 지금 JPA 의존성과 @Entity 코드(Notification)는 남아 있되, 런타임에는 비활성이다.
@SpringBootApplication(
    exclude = [
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
    ],
)
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}
