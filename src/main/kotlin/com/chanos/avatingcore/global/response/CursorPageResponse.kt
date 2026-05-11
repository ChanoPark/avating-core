package com.chanos.avatingcore.global.response

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 커서 기반 페이지네이션 응답 모델
 */
data class CursorPageResponse<T>(
    @Schema(description = "조회된 데이터 목록")
    val content: List<T>,

    @Schema(description = "다음 페이지 커서 (null이면 마지막 페이지)", nullable = true)
    val nextCursor: String?,

    @Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,
) {
    companion object {
        fun <T> of(content: List<T>, nextCursor: String?, hasNext: Boolean): CursorPageResponse<T> =
            CursorPageResponse(content = content, nextCursor = nextCursor, hasNext = hasNext)
        
        fun <T> empty(): CursorPageResponse<T> =
            CursorPageResponse(content = emptyList(), nextCursor = null, hasNext = false)
    }
}
