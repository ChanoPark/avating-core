package com.chanos.avatingcore.auth.service

import com.chanos.avatingcore.auth.dto.request.SignupRequest
import com.chanos.avatingcore.auth.dto.response.AuthTokenResponse

interface AuthService {
    /**
     * 회원가입
     * @param request SignupRequest
     * @return JWT Token Pair
     */
    fun signup(request: SignupRequest): AuthTokenResponse
}
