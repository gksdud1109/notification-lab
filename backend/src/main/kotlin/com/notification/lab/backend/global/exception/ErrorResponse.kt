package com.notification.lab.backend.global.exception

import org.springframework.validation.FieldError

data class ErrorResponse(
    val code: String,
    val message: String,
    val details: List<FieldError>? = null,
) {
    data class FieldError(
        val field: String,
        val message: String,
    )
}