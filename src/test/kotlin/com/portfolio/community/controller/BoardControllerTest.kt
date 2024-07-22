package com.portfolio.community.controller

import com.portfolio.community.dto.board.BoardCreateRequest
import com.portfolio.community.dto.board.BoardResponse
import com.portfolio.community.dto.board.BoardUpdateRequest
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.Role
import com.portfolio.community.service.BoardService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class BoardControllerTest {

    private lateinit var boardController: BoardController
    private lateinit var boardService: BoardService

    private val defaultRole = Role(name = "User", level = 0, id = 1)

    @BeforeEach
    fun setUp() {
        boardService = mockk()
        boardController = BoardController(boardService)
    }

    @Test
    fun given_BoardExists_when_GetAllBoards_then_ReturnOkAndBoardList() {
        //Given
        val boards = (1..10).map { BoardResponse(BoardEntity(name = "Board $it", priority = it, readableRole = defaultRole, id = it.toLong())) }
        every { boardService.getAllBoards() } returns boards

        //When
        val result = boardController.getAllBoards()

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(10, result.body!!.size)
        assertEquals("Board 1", result.body!![0].name)
        assertEquals(1, result.body!![0].priority)
        assertEquals(1, result.body!![0].id)
    }

    @Test
    fun given_NoBoardExists_when_GetAllBoards_then_ReturnNoContent() {
        //Given
        every { boardService.getAllBoards() } returns (listOf())

        //When
        val result = boardController.getAllBoards()

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

    @Test
    fun given_ValidData_when_CreateBoard_then_ReturnCreatedAndNewBoard() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = 1L)
        val newBoard = BoardResponse(BoardEntity(name = "New Board", priority = 0, readableRole = defaultRole, id = 1L))
        every { boardService.createBoard(any()) } returns (newBoard)

        //When
        val result = boardController.createBoard(boardCreateRequest)

        //Then
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals("/boards/1", result.headers.location!!.path)
        assertEquals("New Board", result.body!!.name)
        assertEquals(0, result.body!!.priority)
        assertEquals(1, result.body!!.id)
    }

    @Test
    fun given_ValidData_when_UpdateBoard_then_ReturnOkAndUpdatedBoard() {
        //Given
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 0, readableRoleId = 1L)
        val updatedBoard = BoardResponse(BoardEntity(name = "Updated Board", priority = 0, readableRole = defaultRole, id = 1L))
        every { boardService.updateBoard(1, any()) } returns updatedBoard

        //When
        val result = boardController.updateBoard(1, boardUpdateRequest)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Updated Board", result.body!!.name)
        assertEquals(0, result.body!!.priority)
        assertEquals(1, result.body!!.id)
    }

    @Test
    fun given_ValidData_when_DeleteBoard_then_ReturnNoContent() {
        //Given
        every { boardService.deleteBoard(1) } just runs

        //When
        val result = boardController.deleteBoard(1)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

}