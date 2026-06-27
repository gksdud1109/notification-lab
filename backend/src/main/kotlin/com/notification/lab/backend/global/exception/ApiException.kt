package com.notification.lab.backend.global.exception

class ApiException(
    val code: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message ?: code.defaultMessage, cause) {
    companion object {
        fun of(code: ErrorCode, message: String? = null, cause: Throwable? = null): ApiException {
            return ApiException(code, message, cause)
        }
    }
}