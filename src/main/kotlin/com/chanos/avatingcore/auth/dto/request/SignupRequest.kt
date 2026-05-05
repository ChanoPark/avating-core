package com.chanos.avatingcore.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email(message = "{validation.email.invalid}")
    @field:NotBlank(message = "{validation.email.required}")
    @Schema(description = "이메일 주소", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "{validation.password.required}")
    @Schema(
        description = "/api/crypto/public-key 로 조회한 RSA 공개키로 암호화한 비밀번호 (Base64 인코딩)",
        example = "Base64EncodedRSAEncryptedPassword==",
    )
    val encryptedPassword: String,

    @field:NotBlank(message = "{validation.nickname.required}")
    @field:Size(min = 2, max = 30, message = "{validation.nickname.size}")
    @Schema(description = "닉네임 (2~30자)", example = "닉네임")
    val nickname: String,
)
