package com.chanos.avatingcore.crypto.controller

import com.chanos.avatingcore.crypto.dto.response.PublicKeyResponse
import com.chanos.avatingcore.global.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Crypto", description = "암호화 API")
interface CryptoControllerSpec {

    @Operation(
        summary = "RSA 공개키 조회",
        description = "RSA 암호화할 때 사용하는 공개키를 반환합니다."
    )
    @ApiResponses(io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "공개키 반환 성공"))
    fun getPublicKey(): ApiResponse<PublicKeyResponse>
}
