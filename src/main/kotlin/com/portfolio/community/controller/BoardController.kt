package com.portfolio.community.controller

import com.portfolio.community.dto.board.*
import com.portfolio.community.service.BoardService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
class BoardController(
    private val boardService: BoardService,
    ) {

    @GetMapping("/boards")
    @PreAuthorize("permitAll()")
    fun getAllBoards(): ResponseEntity<List<BoardResponse>> {
        val boards = boardService.getAllBoards()
        return if(boards.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(boards)
        }
    }

    @PostMapping("/boards")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun createBoard(@Valid @RequestBody boardCreateRequest: BoardCreateRequest): ResponseEntity<BoardResponse> {
        val board = boardService.createBoard(boardCreateRequest)
        val location = URI.create("/boards/${board.id}")
        return ResponseEntity.created(location).body(board)
    }

    @PutMapping("/boards/{boardId}")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun updateBoard(@PathVariable boardId: Long, @Valid @RequestBody boardUpdateRequest: BoardUpdateRequest): ResponseEntity<BoardResponse> {
        val board = boardService.updateBoard(boardId, boardUpdateRequest)
        return ResponseEntity.ok(board)
    }

    @DeleteMapping("/boards/{boardId}")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun deleteBoard(@PathVariable boardId: Long): ResponseEntity<Void> {
        boardService.deleteBoard(boardId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/boards/{sourceBoardId}/posts/transfer/{targetBoardId}")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun deleteBoardAndMovePosts(@PathVariable sourceBoardId: Long, @PathVariable targetBoardId: Long): ResponseEntity<BoardResponse> {
        val board = boardService.deleteBoardAndMovePosts(sourceBoardId, targetBoardId)
        return ResponseEntity.ok(board)
    }

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