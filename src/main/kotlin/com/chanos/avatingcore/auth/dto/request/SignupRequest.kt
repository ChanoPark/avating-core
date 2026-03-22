package com.chanos.avatingcore.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignupRequest(
    @field:Email(message = "{validation.email.invalid}")
    @field:NotBlank(message = "{validation.email.required}")
    val email: String,

    @field:NotBlank(message = "{validation.password.required}")
    val encryptedPassword: String,

    @field:NotBlank(message = "{validation.nickname.required}")
    @field:Size(min = 2, max = 30, message = "{validation.nickname.size}")
    val nickname: String,
)
