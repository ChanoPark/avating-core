package com.chanos.avatingcore.auth.dto.response

data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)
