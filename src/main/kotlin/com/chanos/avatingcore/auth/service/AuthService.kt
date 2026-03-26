package com.chanos.avatingcore.auth.service

import com.chanos.avatingcore.auth.dto.request.LoginRequest
import com.chanos.avatingcore.auth.dto.request.SignupRequest
import com.chanos.avatingcore.auth.dto.response.AuthTokenResponse

interface AuthService {
    /**
     * 회원가입
     * @param request SignupRequest
     * @return JWT Token Pair
     */
    fun signup(request: SignupRequest): AuthTokenResponse

    /**
     * 로그인
     * @param request SignupRequest
     * @return JWT Token Pair
     */
    fun login(request: LoginRequest): AuthTokenResponse

    /**
     * Refresh Token으로 Access Token 재발급
     * @param refreshToken Refresh Token
     * @return 새 JWT Token Pair
     */
    fun refresh(refreshToken: String): AuthTokenResponse
}
