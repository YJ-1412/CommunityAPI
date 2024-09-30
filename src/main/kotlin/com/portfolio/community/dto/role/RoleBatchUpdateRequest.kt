package com.portfolio.community.dto.role

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "역할 일괄 수정 요청 DTO")
data class RoleBatchUpdateRequest(
    @Schema(
        description = "수정할 역할 목록. 각 역할의 ID와 함께 수정할 내용을 포함합니다.",
        example = "[[1, { \"name\": \"LV1\", \"level\": 1 }], [2, { \"name\": \"LV2\", \"level\": 2 }]]",
        required = true
    )
    val updates: List<Pair<Long, RoleUpdateRequest>>,
    @Schema(
        description = "생성할 역할 목록. 각 역할의 정보를 포함합니다.",
        example = "[{ \"name\": \"LV0\", \"level\": 0 }]",
        required = true
    )
    val creates: List<RoleCreateRequest>,
    @Schema(
        description = "역할을 삭제하고 역할에 연결된 사용자와 게시판을 다른 역할로 옮깁니다. 각 쌍은 [삭제할 역할 ID, 새로운 역할 ID] 형식입니다.",
        example = "[[3, 1], [4, 2]]",
        required = true
    )
    val moves: List<Pair<Long, Long>>
)
