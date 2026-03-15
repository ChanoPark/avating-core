package com.chanos.avatingcore.crypto.service

import org.springframework.stereotype.Service
import java.security.KeyPair
import java.util.Base64
import javax.crypto.Cipher

/**
 * RSA 공개키 암호화/복호화 서비스.
 */
@Service
class RsaCryptoService(private val rsaKeyPair: KeyPair) {

    /**
     * 공개키 반환
     */
    fun getPublicKeyBase64(): String =
        Base64.getEncoder().encodeToString(rsaKeyPair.public.encoded)

    /**
     * 복호화
     */
    fun decrypt(encryptedBase64: String): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.private)

        val encryptedBytes = Base64.getDecoder().decode(encryptedBase64)
        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }
}