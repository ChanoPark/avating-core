package com.chanos.avatingcore.auth.controller

import com.chanos.avatingcore.auth.dto.request.LoginRequest
import com.chanos.avatingcore.auth.dto.request.SignupRequest
import com.chanos.avatingcore.auth.dto.request.RefreshTokenRequest
import com.chanos.avatingcore.auth.dto.response.AuthTokenResponse
import com.chanos.avatingcore.auth.service.AuthService
import com.chanos.avatingcore.global.response.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) : AuthControllerSpec {

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    override fun signup(@Valid @RequestBody request: SignupRequest): ApiResponse<AuthTokenResponse> =
        ApiResponse.of(authService.signup(request))

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    override fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<AuthTokenResponse> =
        ApiResponse.of(authService.login(request))

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    override fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Valid @RequestBody refreshTokenRequest: RefreshTokenRequest,
    ): ApiResponse<AuthTokenResponse> =
        ApiResponse.of(authService.refresh(refreshTokenRequest.refreshToken))
}
