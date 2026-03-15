package com.chanos.avatingcore.global.response

/**
 * API 응답 모델
 */
data class ApiResponse<T>(
    val data: T,
) {
    companion object {
        fun <T> of(data: T): ApiResponse<T> = ApiResponse(data)
    }
}
