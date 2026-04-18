package com.chanos.avatingcore.avatar.entity.enums

enum class AvatarType {
    SURVEY,             // 설문을 통해 생성됨
    EXTERNAL_SERVICE,   // Custom GPTs와 같이 연동됨
    CUSTOM_PROMPT,      // 프롬프트 입력
    ;
}
