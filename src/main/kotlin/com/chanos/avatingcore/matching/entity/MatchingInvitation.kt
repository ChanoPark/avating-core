package com.chanos.avatingcore.matching.entity

import com.chanos.avatingcore.avatar.entity.Avatar
import com.chanos.avatingcore.global.entity.BaseEntity
import com.chanos.avatingcore.matching.vo.MatchingInvitationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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

    @Column(name = "expired_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    var expiredAt: OffsetDateTime

) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null

    companion object {
        fun createInvitation(inviterAvatar: Avatar, inviteeAvatar: Avatar, requestMessage: String? = null) =
            MatchingInvitation(
                inviterAvatar = inviterAvatar,
                inviteeAvatar = inviteeAvatar,
                status = MatchingInvitationStatus.PENDING,
                requestMessage = requestMessage,
                rejectMessage = null,
                expiredAt = OffsetDateTime.now().plusDays(1),
            )
    }
}
