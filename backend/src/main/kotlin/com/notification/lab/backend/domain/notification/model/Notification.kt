package com.notification.lab.backend.domain.notification.model

import com.notification.lab.backend.global.jpa.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Notification(

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var payload: String,
) : BaseEntity() {
}