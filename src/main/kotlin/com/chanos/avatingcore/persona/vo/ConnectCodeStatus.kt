package com.chanos.avatingcore.persona.vo

enum class ConnectCodeStatus {
    ISSUED,         // 생성
    COLLECTING,     // 수집 중
    COLLECTED,      // 수집 완료
    DELETED,        // 삭제됨
    DONE,           // 사용 완료 (새로운 연결 코드로 수집된 상태)
}
