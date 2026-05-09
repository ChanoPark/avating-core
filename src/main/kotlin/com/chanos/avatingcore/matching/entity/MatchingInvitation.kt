package com.chanos.avatingcore.matching.entity

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.global.entity.BaseUUIDEntity
import com.chanos.avatingcore.matching.exception.MatchingErrorCode.*
import com.chanos.avatingcore.matching.exception.MatchingException
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus.*
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

    /** 매칭 초대 거절 */
    fun reject(rejectMessage: String) {
        when (status) {
            ACCEPTED  -> throw MatchingException.of(FAILED_REJECT_MATCHING_INVITATION_STATUS_ACCEPTED)
            MATCHING  -> throw MatchingException.of(FAILED_REJECT_MATCHING_INVITATION_STATUS_MATCHING)
            REJECTED  -> throw MatchingException.of(FAILED_REJECT_MATCHING_INVITATION_STATUS_REJECTED)
            CANCELED  -> throw MatchingException.of(FAILED_REJECT_MATCHING_INVITATION_STATUS_CANCELED)
            ABORTED   -> throw MatchingException.of(FAILED_REJECT_MATCHING_INVITATION_STATUS_ABORTED)
            DONE      -> throw MatchingException.of(FAILED_REJECT_MATCHING_INVITATION_STATUS_DONE)
            PENDING   -> {
                this.rejectMessage = rejectMessage
                this.status = REJECTED
            }
        }
    }

    /** 매칭 초대 취소 */
    fun cancel() {
        when (status) {
            ACCEPTED  -> throw MatchingException.of(FAILED_CANCEL_MATCHING_INVITATION_STATUS_ACCEPTED)
            MATCHING  -> throw MatchingException.of(FAILED_CANCEL_MATCHING_INVITATION_STATUS_MATCHING)
            REJECTED  -> throw MatchingException.of(FAILED_CANCEL_MATCHING_INVITATION_STATUS_REJECTED)
            CANCELED  -> throw MatchingException.of(FAILED_CANCEL_MATCHING_INVITATION_STATUS_CANCELED)
            ABORTED   -> throw MatchingException.of(FAILED_CANCEL_MATCHING_INVITATION_STATUS_ABORTED)
            DONE      -> throw MatchingException.of(FAILED_CANCEL_MATCHING_INVITATION_STATUS_DONE)
            PENDING   -> this.status = CANCELED
        }
    }

    /** 초대한 사용자 확인 */
    fun isInviter(memberId: UUID): Boolean = inviterAvatar.member.id == memberId

    /** 초대 받은 사용자 확인 */
    fun isInvitee(memberId: UUID): Boolean = inviteeAvatar.member.id == memberId
}
