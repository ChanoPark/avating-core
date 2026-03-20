package com.chanos.avatingcore.crypto.controller

import com.chanos.avatingcore.crypto.dto.response.PublicKeyResponse
import com.chanos.avatingcore.crypto.service.RsaCryptoService
import com.chanos.avatingcore.global.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/crypto")
class CryptoController(
    private val rsaCryptoService: RsaCryptoService,
) : CryptoControllerSpec {

    @GetMapping("/public-key")
    override fun getPublicKey(): ApiResponse<PublicKeyResponse> = ApiResponse.of(PublicKeyResponse.of(rsaCryptoService.getPublicKeyBase64()))
}
