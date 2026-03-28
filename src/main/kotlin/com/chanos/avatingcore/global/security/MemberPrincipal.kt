package com.chanos.avatingcore.global.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import java.util.UUID

data class MemberPrincipal(val memberId: UUID) : Authentication {
    override fun getName(): String = memberId.toString()
    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
    override fun getCredentials(): Any? = null
    override fun getDetails(): Any? = null
    override fun getPrincipal(): Any = this
    override fun isAuthenticated(): Boolean = true
    override fun setAuthenticated(isAuthenticated: Boolean) = require(isAuthenticated) { "Cannot set this token to unauthenticated" }
}
