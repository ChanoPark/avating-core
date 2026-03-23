package com.chanos.avatingcore.auth.service

import com.chanos.avatingcore.auth.dto.request.LoginRequest
import com.chanos.avatingcore.auth.dto.request.SignupRequest
import com.chanos.avatingcore.auth.dto.response.AuthTokenResponse
import com.chanos.avatingcore.auth.exception.AuthErrorCode
import com.chanos.avatingcore.auth.exception.AuthException
import com.chanos.avatingcore.auth.repository.RefreshTokenRepository
import com.chanos.avatingcore.crypto.service.RsaCryptoService
import com.chanos.avatingcore.auth.jwt.JwtProvider
import com.chanos.avatingcore.auth.vo.MemberAuthInfo
import com.chanos.avatingcore.global.util.logger
import com.chanos.avatingcore.member.service.MemberService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthServiceImpl(
    private val memberService: MemberService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val rsaCryptoService: RsaCryptoService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
) : AuthService {

    private val log = logger()

    @Transactional
    override fun signup(request: SignupRequest): AuthTokenResponse {
        val rawPassword = decryptPassword(request.encryptedPassword)
        validatePassword(rawPassword)

        // 회원 유효성 검사
        memberService.validateNewMember(request.email, request.nickname)

        // 회원 생성
        val memberAuthInfo: MemberAuthInfo = memberService.createMember(
            email = request.email,
            hashedPassword = passwordEncoder.encode(rawPassword)!!,
            nickname = request.nickname,
        )

        log.debug("member_created memberAuthInfo={}", memberAuthInfo)

        // 토큰 발급
        val authTokenResponse: AuthTokenResponse = issueTokenPair(memberAuthInfo.memberId)
        updateRefreshToken(memberAuthInfo.memberId, authTokenResponse.refreshToken)

        return authTokenResponse
    }

    override fun login(request: LoginRequest): AuthTokenResponse {
        val memberAuthInfo: MemberAuthInfo = memberService.findMemberAuthInfo(request.email)
            ?: throw AuthException.of(AuthErrorCode.NOT_FOUND_MEMBER)

        // 비밀번호 확인
        val rawPassword = decryptPassword(request.encryptedPassword)
        if (isInvalidPassword(rawPassword, memberAuthInfo.password)) {
            throw AuthException.of(AuthErrorCode.INVALID_PASSWORD)
        }

        log.debug("member_login memberAuthInfo={}", memberAuthInfo)

        // 토큰 발급
        val authTokenResponse: AuthTokenResponse = issueTokenPair(memberAuthInfo.memberId)
        updateRefreshToken(memberAuthInfo.memberId, authTokenResponse.refreshToken)

        return authTokenResponse
    }

    /**
     * 비밀번호 복호화 (RSA)
     */
    private fun decryptPassword(encryptedBase64: String): String =
        runCatching { rsaCryptoService.decrypt(encryptedBase64) }
            .onFailure { log.debug("decrypt_password_failed reason={}", it.message) }
            .getOrElse { throw AuthException(AuthErrorCode.RSA_DECRYPTION_FAILED) }

    /**
     * 비밀번호 유효성 검사
     */
    private fun validatePassword(password: String) {
        // 비밀번호 길이 확인
        if (password.length !in 8..128) {
            throw AuthException(AuthErrorCode.WEAK_PASSWORD_LENGTH)
        }

        // 비밀번호 요구사항 확인 (영문자, 특수기호를 반드시 포함)
        val hasLetter = password.any { it in 'a'..'z' || it in 'A'..'Z' }
        val hasDigit = password.any { it in '0'..'9' }
        val hasSpecial = password.any { it.code in 33..126 && !it.isLetterOrDigit() }

        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw AuthException(AuthErrorCode.WEAK_PASSWORD_COMPLEXITY)
        }
    }

    /**
     * JWT Token 발급 (Access, Refresh)
     */
    private fun issueTokenPair(memberId: UUID): AuthTokenResponse {
        val accessToken = jwtProvider.generateAccessToken(memberId)
        val refreshToken = jwtProvider.generateRefreshToken(memberId)

        return AuthTokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProvider.accessTokenExpirySeconds,
        )
    }

    /**
     * Refresh Token을 Valkey에 저장
     */
    private fun updateRefreshToken(memberId: UUID, refreshToken: String) {
        refreshTokenRepository.save(
            memberId = memberId,
            token = refreshToken,
            expirySeconds = jwtProvider.refreshTokenExpirySeconds,
        )
    }

    /**
     * 올바른 비밀번호인지 확인
     */
    private fun isInvalidPassword(rawPassword: String, hashedPassword: String): Boolean =
        !passwordEncoder.matches(rawPassword, hashedPassword)
}
