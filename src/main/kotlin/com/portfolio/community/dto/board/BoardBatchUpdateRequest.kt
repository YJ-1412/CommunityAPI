package com.portfolio.community.dto.board

data class BoardBatchUpdateRequest(
    val updates: List<Pair<Long, BoardUpdateRequest>>,
    val creates: List<BoardCreateRequest>,
    val deletes: List<Long>,
    val moves: List<Pair<Long, Long>>
)
