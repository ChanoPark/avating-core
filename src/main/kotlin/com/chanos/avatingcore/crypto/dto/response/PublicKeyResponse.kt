package com.chanos.avatingcore.crypto.dto.response

data class PublicKeyResponse(
    val publicKey: String,
) {
    companion object {
        fun of(publicKey: String): PublicKeyResponse = PublicKeyResponse(publicKey)
    }
}
