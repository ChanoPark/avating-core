package com.chanos.avatingcore.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class AuthTokenResponse(
    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @Schema(description = "JWT Refresh Token (Access Token 재발급에 사용)", example = "eyJhbGciOiJIUzI1NiJ9...")
    val refreshToken: String,

    @Schema(description = "토큰 유형", example = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "Access Token 만료까지 남은 시간 (초)", example = "3600")
    val expiresIn: Long,
)
