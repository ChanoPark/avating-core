package com.chanos.avatingcore.crypto.controller

import com.chanos.avatingcore.crypto.dto.response.PublicKeyResponse
import com.chanos.avatingcore.crypto.service.RsaCryptoService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/crypto")
class CryptoController(
    private val rsaCryptoService: RsaCryptoService,
) : CryptoControllerSpec {

    override fun getPublicKey(): PublicKeyResponse = PublicKeyResponse(publicKey = rsaCryptoService.getPublicKeyBase64())
}
