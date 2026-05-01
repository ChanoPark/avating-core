package com.chanos.avatingcore.persona.entity

import com.chanos.avatingcore.global.entity.BaseEntity
import com.chanos.avatingcore.persona.vo.ConnectCodeStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "connect_codes")
class ConnectCode(
    @Column(name = "member_id", nullable = false)
    val memberId: UUID,

    @Column(name = "connect_code", nullable = false)
    var connectCode: String,

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "connect_code_status", nullable = false, length = 20)
    var connectCodeStatus: ConnectCodeStatus = ConnectCodeStatus.ISSUED

    companion object {
        const val STORE_TTL_SECONDS = 900L

        fun of(memberId: UUID, connectCode: String): ConnectCode {
            return ConnectCode(
                memberId = memberId,
                connectCode = connectCode,
            )
        }

        /** 인증 코드 생성 */
        fun generateConnectCode(email: String, uuid: UUID): String {
            return "${email}-$uuid"
        }
    }

    /** 수집 시작 */
    fun collecting() {
        this.connectCodeStatus = ConnectCodeStatus.COLLECTING
    }

    /** 수집 완료 */
    fun collected() {
        this.connectCodeStatus = ConnectCodeStatus.COLLECTED
    }

    /** 삭제 */
    fun delete() {
        this.connectCodeStatus = ConnectCodeStatus.DELETED
    }
}
