package com.chanos.avatingcore.persona.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

data class ConnectCodeResponse(
    @Schema(description = "Custom GPTs 연동에 사용하는 연결 코드", example = "A1B2C3D4")
    val connectCode: String,

    @Schema(description = "만료까지 남은 시간 (초)", example = "600")
    val expiresIn: Long,

    @Schema(description = "만료 일시 (ISO 8601)", example = "2026-05-05T18:00:00+09:00")
    val expiresAt: OffsetDateTime,
)
