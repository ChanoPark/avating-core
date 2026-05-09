package com.chanos.avatingcore.matching.entity

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.global.entity.BaseUUIDEntity
import com.chanos.avatingcore.matching.exception.MatchingException
import com.chanos.avatingcore.matching.vo.MatchingAction
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.ACCEPTED
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.CANCELED
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.PENDING
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.REJECTED
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "matching_invitations")
class MatchingInvitation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_avatar_id", nullable = false)
    val inviterAvatar: Avatar,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_avatar_id", nullable = false)
    val inviteeAvatar: Avatar,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: MatchingInvitationStatus,

    @Column(name = "request_message", length = 300)
    var requestMessage: String? = null,

    @Column(name = "reject_message", length = 300)
    var rejectMessage: String? = null,

    @Column(name = "expired_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    var expiredAt: OffsetDateTime

) : BaseUUIDEntity() {

    companion object {
        fun createInvitation(inviterAvatar: Avatar, inviteeAvatar: Avatar, requestMessage: String? = null) =
            MatchingInvitation(
                inviterAvatar = inviterAvatar,
                inviteeAvatar = inviteeAvatar,
                status = PENDING,
                requestMessage = requestMessage,
                rejectMessage = null,
                expiredAt = OffsetDateTime.now().plusDays(1),
            )
    }

    /** 매칭 초대 수락 */
    fun accept() {
        if (status != PENDING) {
            throw MatchingException.forInvalidInvitationStatus(status, MatchingAction.ACCEPT)
        }
        this.status = ACCEPTED
    }

    /** 매칭 초대 거절 */
    fun reject(rejectMessage: String) {
        if (status != PENDING) {
            throw MatchingException.forInvalidInvitationStatus(status, MatchingAction.REJECT)
        }
        this.rejectMessage = rejectMessage
        this.status = REJECTED
    }

    /** 매칭 초대 취소 */
    fun cancel() {
        if (status != PENDING) {
            throw MatchingException.forInvalidInvitationStatus(status, MatchingAction.CANCEL)
        }
        this.status = CANCELED
    }

    /** 초대한 사용자 확인 */
    fun isInviter(memberId: UUID): Boolean = inviterAvatar.member.id == memberId

    /** 초대 받은 사용자 확인 */
    fun isInvitee(memberId: UUID): Boolean = inviteeAvatar.member.id == memberId
}
