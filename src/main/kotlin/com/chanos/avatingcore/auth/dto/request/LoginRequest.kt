package com.chanos.avatingcore.auth.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class LoginRequest(
    @field:Email(message = "{validation.email.invalid}")
    @field:NotBlank(message = "{validation.email.required}")
    val email: String,

    @field:NotBlank(message = "{validation.password.required}")
    val encryptedPassword: String,
)
