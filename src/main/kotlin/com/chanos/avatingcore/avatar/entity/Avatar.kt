package com.chanos.avatingcore.avatar.entity

import com.chanos.avatingcore.avatar.entity.enums.AvatarType
import com.chanos.avatingcore.avatar.entity.enums.SourceType
import com.chanos.avatingcore.global.entity.BaseEntity
import com.chanos.avatingcore.member.entity.Member
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
import java.util.UUID

@Entity
@Table(name = "avatars")
class Avatar(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_type", nullable = false, length = 20)
    val avatarType: AvatarType,

    @Column(name = "name", nullable = false, length = 50)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    val sourceType: SourceType,

    @Column(name = "description", nullable = true, length = 200)
    val description: String? = null,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null

    companion object {
        fun of(
            member: Member,
            avatarType: AvatarType,
            name: String,
            sourceType: SourceType,
            description: String? = null,
        ): Avatar {
            return Avatar(
                member = member,
                avatarType = avatarType,
                name = name,
                sourceType = sourceType,
                description = description,
            )
        }
    }
}
