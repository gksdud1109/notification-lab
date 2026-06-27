package com.notification.lab.backend.global.jpa.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @CreatedDate
    var createdAt: Instant = Instant.EPOCH
        protected set

    @LastModifiedDate
    var updatedAt: Instant = Instant.EPOCH
        protected set

    var deleted: Boolean = false
        protected set

    /**
     * 영속화 이후 보장되는 id를 반환한다. 아직 할당 전(미영속)이면 예외를 던진다.
     * 흩어져 있던 `id ?: error("...is null after persist")` 처리를 한 곳으로 모은다.
     */
    fun requireId(): Long = id ?: error("${this::class.simpleName} id is not assigned yet (not persisted?)")

    /** Mark this aggregate as soft-deleted. Caller is responsible for persisting. */
    fun markDeleted() {
        this.deleted = true
    }

}