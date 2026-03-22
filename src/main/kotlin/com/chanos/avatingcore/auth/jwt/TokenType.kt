package com.chanos.avatingcore.auth.jwt

enum class TokenType(
    val value: String,
) {
    ACCESS("access"),
    REFRESH("refresh"),
}
