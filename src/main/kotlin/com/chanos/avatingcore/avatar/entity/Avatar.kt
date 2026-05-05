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
import jakarta.persistence.Version
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

    @Column(name = "name", nullable = false, unique = true, length = 50)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    val sourceType: SourceType,

    @Column(name = "description", nullable = true, length = 200)
    val description: String? = null,

    @Column(name = "is_primary", nullable = false)
    var isPrimary: Boolean = false,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null

    @Version
    @Column(name = "version", nullable = false)
    var version: Long = 0

    companion object {
        fun of(
            member: Member,
            avatarType: AvatarType,
            name: String,
            sourceType: SourceType,
            description: String? = null,
            isPrimary: Boolean = false,
        ): Avatar {
            return Avatar(
                member = member,
                avatarType = avatarType,
                name = name,
                sourceType = sourceType,
                description = description,
                isPrimary = isPrimary,
            )
        }
    }

    /** 대표 아바타 활성화 */
    fun activatePrimary() {
        this.isPrimary = true
    }

    /** 대표 아바타 비활성화 */
    fun deactivatePrimary() {
        this.isPrimary = false
    }
}
