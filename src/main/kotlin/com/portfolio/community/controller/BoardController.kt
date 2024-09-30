package com.portfolio.community.controller

import com.portfolio.community.dto.board.*
import com.portfolio.community.service.BoardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@Tag(name = "boards", description = "게시판 API")
@RestController
class BoardController(
    private val boardService: BoardService
) {

    @Operation(
        summary = "게시판 생성",
        description = "새로운 게시판을 생성합니다. 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "게시판 생성 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = BoardCreateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "201", description = "게시판 생성 성공", content = [Content(schema = Schema(implementation = BoardResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자/스태프 권한 필요"),
            ApiResponse(responseCode = "404", description = "유효하지 않은 readableRoleId")
        ]
    )
    @PostMapping("/boards")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun createBoard(@Valid @RequestBody boardCreateRequest: BoardCreateRequest): ResponseEntity<BoardResponse> {
        val board = boardService.createBoard(boardCreateRequest)
        val location = URI.create("/boards/${board.id}")
        return ResponseEntity.created(location).body(board)
    }

    @Operation(
        summary = "모든 게시판 조회",
        description = "등록된 모든 게시판 목록을 조회합니다.",
        responses = [
            ApiResponse(responseCode = "200", description = "게시판 목록 조회 성공", content = [Content(schema = Schema(implementation = BoardResponse::class, type = "array"), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "게시판 목록이 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @GetMapping("/boards")
    fun getAllBoards(): ResponseEntity<List<BoardResponse>> {
        val boards = boardService.getAllBoards()
        return if(boards.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(boards)
        }
    }

    @Operation(
        summary = "게시판 수정",
        description = "특정 게시판의 정보를 수정합니다. 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "boardId", description = "수정할 게시판의 ID", required = true, example = "1")
        ],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "게시판 수정 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = BoardUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "게시판 수정 성공", content = [Content(schema = Schema(implementation = BoardResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자/스태프 권한 필요"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음 또는 역할을 찾을 수 없음")
        ]
    )
    @PutMapping("/boards/{boardId}")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun updateBoard(@PathVariable boardId: Long, @Valid @RequestBody boardUpdateRequest: BoardUpdateRequest): ResponseEntity<BoardResponse> {
        val board = boardService.updateBoard(boardId, boardUpdateRequest)
        return ResponseEntity.ok(board)
    }

    @Operation(
        summary = "게시판 삭제",
        description = "특정 게시판을 삭제합니다. 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "boardId", description = "삭제할 게시판의 ID", required = true, example = "1")
        ],
        responses = [
            ApiResponse(responseCode = "204", description = "게시판 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자/스태프 권한 필요"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
        ]
    )
    @DeleteMapping("/boards/{boardId}")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun deleteBoard(@PathVariable boardId: Long): ResponseEntity<Void> {
        boardService.deleteBoard(boardId)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "게시판 삭제 및 게시글 이동",
        description = "특정 게시판을 삭제하고, 해당 게시판의 게시글을 다른 게시판으로 이동합니다. 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        parameters = [
            Parameter(name = "sourceBoardId", description = "삭제할 게시판의 ID", required = true, example = "1"),
            Parameter(name = "targetBoardId", description = "게시글을 이동할 대상 게시판의 ID", required = true, example = "2")
        ],
        responses = [
            ApiResponse(responseCode = "200", description = "게시판 삭제 및 게시글 이동 성공", content = [Content(schema = Schema(implementation = BoardResponse::class), mediaType = "application/json")]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자/스태프 권한 필요"),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음")
        ]
    )
    @DeleteMapping("/boards/{sourceBoardId}/posts/transfer/{targetBoardId}")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun deleteBoardAndMovePosts(@PathVariable sourceBoardId: Long, @PathVariable targetBoardId: Long): ResponseEntity<BoardResponse> {
        val board = boardService.deleteBoardAndMovePosts(sourceBoardId, targetBoardId)
        return ResponseEntity.ok(board)
    }

    @Operation(
        summary = "게시판 일괄 수정",
        description = "여러 게시판을 일괄적으로 생성, 수정, 삭제합니다. 관리자/스태프 권한이 필요합니다.",
        security = [SecurityRequirement(name = "Bearer Authentication")],
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "게시판 일괄 수정 요청 DTO",
            required = true,
            content = [Content(schema = Schema(implementation = BoardBatchUpdateRequest::class))]
        ),
        responses = [
            ApiResponse(responseCode = "200", description = "게시판 일괄 수정 성공", content = [Content(schema = Schema(implementation = BoardResponse::class, type = "array"), mediaType = "application/json")]),
            ApiResponse(responseCode = "204", description = "모든 게시판 삭제", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "403", description = "권한 부족 - 관리자/스태프 권한 필요", content = [Content(schema = Schema(implementation = Void::class))]),
            ApiResponse(responseCode = "404", description = "게시판을 찾을 수 없음 또는 역할을 찾을 수 없음", content = [Content(schema = Schema(implementation = Void::class))])
        ]
    )
    @PutMapping("/boards")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun batchUpdateBoard(@RequestBody boardBatchUpdateRequest: BoardBatchUpdateRequest): ResponseEntity<List<BoardResponse>> {
        val boards = boardService.batchUpdateBoard(boardBatchUpdateRequest)
        return if(boards.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(boards)
        }
    }

}