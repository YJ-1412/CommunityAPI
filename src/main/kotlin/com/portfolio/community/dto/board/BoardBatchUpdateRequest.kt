package com.portfolio.community.dto.board

import io.swagger.v3.oas.annotations.media.Schema

data class BoardBatchUpdateRequest(
    @Schema(
        description = "수정할 게시판 목록. 각 게시판의 ID와 함께 수정할 내용을 포함합니다.",
        example = "[[1, { \"name\": \"유머게시판\", \"priority\": 1, \"readableRoleId\": 1 }]]",
        required = true
    )
    val updates: List<Pair<Long, BoardUpdateRequest>>,
    @Schema(
        description = "생성할 게시판 목록. 각 게시판의 정보를 포함합니다.",
        example = "[{ \"name\": \"상담게시판\", \"priority\": 0, \"readableRoleId\": 1 }]",
        required = true
    )
    val creates: List<BoardCreateRequest>,
    @Schema(
        description = "삭제할 게시판 ID 목록. 각 ID는 삭제할 게시판을 나타냅니다. 게시판에 등록된 게시글도 함께 삭제됩니다.",
        example = "[2, 3]",
        required = true
    )
    val deletes: List<Long>,
    @Schema(
        description = "특정 게시판을 삭제하고, 해당 게시판의 게시글을 다른 게시판으로 이동합니다.. 각 쌍은 [삭제할 게시판 ID, 대상 게시판 ID] 형식입니다.",
        example = "[[4, 1], [5, 1]]",
        required = true
    )
    val moves: List<Pair<Long, Long>>
)
