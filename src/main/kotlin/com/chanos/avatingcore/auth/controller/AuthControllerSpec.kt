package com.chanos.avatingcore.auth.controller

import com.chanos.avatingcore.auth.dto.request.LoginRequest
import com.chanos.avatingcore.auth.dto.request.SignupRequest
import com.chanos.avatingcore.auth.dto.request.RefreshTokenRequest
import com.chanos.avatingcore.auth.dto.response.AuthTokenResponse
import com.chanos.avatingcore.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Auth", description = "인증 API")
interface AuthControllerSpec {

    @Operation(
        summary = "회원가입",
        description = "이메일 + RSA 암호화된 비밀번호로 계정을 생성하고 토큰을 발급합니다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "201", description = "회원가입 성공"),
        SwaggerApiResponse(responseCode = "400", description = "잘못된 입력"),
        SwaggerApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복"),
        SwaggerApiResponse(responseCode = "422", description = "비밀번호 정책 위반"),
    )
    fun signup(@RequestBody @Valid request: SignupRequest): ApiResponse<AuthTokenResponse>

    @Operation(
        summary = "로그인",
        description = "이메일 + RSA 암호화된 비밀번호로 인증하고 토큰을 발급합니다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "로그인 성공"),
        SwaggerApiResponse(responseCode = "400", description = "잘못된 입력"),
        SwaggerApiResponse(responseCode = "404", description = "회원 없음 또는 비밀번호 불일치"),
        SwaggerApiResponse(responseCode = "422", description = "RSA 복호화 실패"),
    )
    fun login(@RequestBody @Valid request: LoginRequest): ApiResponse<AuthTokenResponse>

    @Operation(
        summary = "Access Token 재발급",
        description = "Refresh Token으로 새 Access Token과 Refresh Token을 발급합니다. Request Body의 refreshToken 필드로 전달합니다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        SwaggerApiResponse(responseCode = "400", description = "Refresh Token 누락"),
        SwaggerApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token"),
    )
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Valid @RequestBody refreshTokenRequest: RefreshTokenRequest,
    ): ApiResponse<AuthTokenResponse>
}
