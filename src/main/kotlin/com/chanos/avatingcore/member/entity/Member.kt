package com.chanos.avatingcore.member.entity

import com.chanos.avatingcore.global.entity.BaseUUIDEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class Member(
    @Column(name = "email", length = 320, nullable = false, unique = true)
    val email: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "nickname", length = 30, nullable = false, unique = true)
    var nickname: String,
) : BaseUUIDEntity()
