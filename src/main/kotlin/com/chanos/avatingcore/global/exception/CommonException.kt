package com.chanos.avatingcore.global.exception

/**
 * 모든 예외의 기반 클래스.
 * 예외 클래스는 이 클래스를 상속하고, 자신의 ErrorCode enum 항목을 전달한다.
 */
open class CommonException(
    val errorCode: ErrorCode,
    message: String = errorCode.reason,
) : RuntimeException(message)
