package com.notification.lab.backend.global.response

data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> ok(data: T?): ApiResponse<T> = ApiResponse(code = "200", message = "OK", data = data)
    }
}