package com.chanos.avatingcore.simulation.dto.request

import com.chanos.avatingcore.simulation.vo.InvitationDirection
import com.chanos.avatingcore.simulation.vo.InvitationStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class InvitationHistoryRequest(

    @Schema(description = "한 페이지에 조회할 항목 수 (1~50)", example = "10", defaultValue = "10")
    @field:Min(value = 1, message = "size는 최소 1 이상이어야 합니다.")
    @field:Max(value = 50, message = "size는 최대 50 이하여야 합니다.")
    val size: Int = 10,

    @Schema(
        description = "초대 상태 필터 (null 이면 전체 조회)",
        example = "PENDING",
        nullable = true,
        allowableValues = ["PENDING", "ACCEPTED", "IN_PROGRESS", "REJECTED", "CANCELED", "ABORTED", "DONE"],
    )
    val status: InvitationStatus? = null,

    @Schema(
        description = "초대 방향 필터 (SENT: 보낸 초대, RECEIVED: 받은 초대) — 필수값",
        example = "SENT",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = ["SENT", "RECEIVED"],
    )
    @field:NotNull(message = "direction 은 필수값입니다. SENT 또는 RECEIVED 중 하나를 지정해 주세요.")
    val direction: InvitationDirection,

    @Schema(
        description = "커서 값 — 이전 응답의 nextCursor 를 그대로 사용한다. null 이면 첫 페이지.",
        example = "eyJjcmVhdGVkQXQiOiIyMDI2LTA1LTA5VDEyOjAwOjAwKzA5OjAwIiwiaWQiOiJ1dWlkIn0",
        nullable = true,
    )
    val cursor: String? = null,
)
