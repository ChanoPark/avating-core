package com.chanos.avatingcore.member.exception

import com.chanos.avatingcore.global.exception.CommonException

class MemberException(
    errorCode: MemberErrorCode,
    message: String = errorCode.reason,
) : CommonException(errorCode, message)
