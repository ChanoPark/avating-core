package com.chanos.avatingcore.global.exception

import org.springframework.http.HttpStatus

/**
 * 에러 코드 인터페이스.
 * 구현체는 각 도메인 패키지 내 enum으로 정의한다.
 */
interface ErrorCode {
    val status: HttpStatus
    val code: String
    val message: String
}
