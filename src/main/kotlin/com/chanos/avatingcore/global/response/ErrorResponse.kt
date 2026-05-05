package com.chanos.avatingcore.global.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(description = "API 에러 응답")
data class ErrorResponse(
    @Schema(description = "에러 코드 (형식: {도메인}_{HTTP상태}_{순번})", example = "MEMBER_409_001")
    val code: String,

    @Schema(description = "에러 메시지", example = "이미 사용 중인 이메일입니다.")
    val message: String,

    @Schema(description = "에러 발생 시각 (ISO 8601)", example = "2026-05-05T12:00:00+09:00")
    val timestamp: OffsetDateTime,

    @Schema(description = "유효성 검사 실패 시 필드별 상세 오류 목록")
    val errors: List<FieldError> = emptyList(),
) {
    companion object {
        fun of(
            code: String,
            message: String,
            errors: List<FieldError> = emptyList(),
        ) = ErrorResponse(
            code = code,
            message = message,
            timestamp = OffsetDateTime.now(),
            errors = errors,
        )
    }

    @Schema(description = "필드 유효성 검사 오류")
    data class FieldError(
        @Schema(description = "오류가 발생한 필드명", example = "email")
        val field: String,

        @Schema(description = "오류 메시지", example = "올바른 이메일 형식이 아닙니다.")
        val message: String,
    )
}
