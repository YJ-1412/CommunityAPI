package com.portfolio.community.service

import com.portfolio.community.dto.board.*
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.RoleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull

class BoardServiceTest {

    private lateinit var boardService: BoardService
    private lateinit var boardRepository: BoardRepository
    private lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setUp() {
        boardRepository = mockk()
        roleRepository = mockk()
        boardService = BoardService(boardRepository, roleRepository)
    }

    @Test
    @DisplayName("Given boards exists When getAllBoards Then return all boards in priority order")
    fun given_BoardExists_when_GetAllBoards_then_ReturnAllBoards() {
        // Given
        val role = RoleEntity(name = "User", level = 0)
        val boards = listOf(
            BoardEntity(name = "Board 1", priority = 1, role),
            BoardEntity(name = "Board 2", priority = 2, role)
        )
        every { boardRepository.findAllByOrderByPriority() } returns boards

        // When
        val result = boardService.getAllBoards()

        // Then
        assertEquals(2, result.size)
        assertEquals("Board 1", result[0].name)
        assertEquals(1, result[0].priority)
        assertEquals("Board 2", result[1].name)
        assertEquals(2, result[1].priority)
    }
    @Test
    @DisplayName("Given valid board data When createBoard Then create and return new board")
    fun given_ValidBoardData_when_CreateBoard_then_CreateAndReturnNewBoard() {
        // Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 1, readableRoleId = 1L)
        val role = RoleEntity(name = "User", level = 0)
        val board = BoardEntity(name = "New Board", priority = 1, readableRole = role)
        every { roleRepository.findByIdOrNull(1L) } returns role
        every { boardRepository.existsByName("New Board") } returns false
        every { boardRepository.existsByPriority(1) } returns false
        every { boardRepository.save(any<BoardEntity>()) } returns board

        // When
        val result = boardService.createBoard(boardCreateRequest)

        // Then
        assertEquals("New Board", result.name)
        verify(exactly = 1) { boardRepository.save(any()) }
    }

    @Test
    @DisplayName("Given duplicate board name When createBoard Then throw IllegalArgumentException")
    fun given_DuplicateBoardName_when_CreateBoard_then_ThrowIllegalArgumentException() {
        // Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 1, readableRoleId = 1L)
        every { boardRepository.existsByName("New Board") } returns true

        // When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.createBoard(boardCreateRequest)
        }
        assertEquals("Board with name already exists", ex.message)
    }

    @Test
    @DisplayName("Given duplicate board priority When createBoard Then throw IllegalArgumentException")
    fun given_DuplicateBoardPriority_when_CreateBoard_then_ThrowIllegalArgumentException() {
        // Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 1, readableRoleId = 1L)
        every { boardRepository.existsByName("New Board") } returns false
        every { boardRepository.existsByPriority(1) } returns true

        // When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.createBoard(boardCreateRequest)
        }
        assertEquals("Board with priority already exists", ex.message)
    }

    @Test
    @DisplayName("Given invalid readableRoleId When createBoard Then throw NotFoundException")
    fun given_InvalidReadableRole_when_CreateBoard_then_ThrowNotFoundException() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 1, readableRoleId = 1L)
        every { boardRepository.existsByName("New Board") } returns false
        every { boardRepository.existsByPriority(1) } returns false
        every { roleRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.createBoard(boardCreateRequest)
        }
        assertEquals("Role with ID 1 Not Found", ex.message)
    }

    @Test
    @DisplayName("Given valid board data When updateBoard Then update and return updated board")
    fun given_ValidBoardData_when_UpdateBoard_then_UpdateAndReturnUpdatedBoard() {
        // Given
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 2, readableRoleId = 1L)
        val role = RoleEntity(name = "User", level = 0)
        val board = BoardEntity(name = "Old Board", priority = 1, readableRole = role)
        every { boardRepository.findByIdOrNull(1L) } returns board
        every { boardRepository.findByName("Updated Board") } returns null
        every { boardRepository.findByPriority(2) } returns null
        every { roleRepository.findByIdOrNull(1L) } returns role

        // When
        val result = boardService.updateBoard(1L, boardUpdateRequest)

        // Then
        assertNotNull(result)
        assertEquals("Updated Board", result.name)
        assertEquals(2, result.priority)
    }

    @Test
    @DisplayName("Given non-existing board When updateBoard Then throw NotFoundException")
    fun given_NonExistingBoard_when_UpdateBoard_then_ThrowNotFoundException() {
        // Given
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 1, readableRoleId = 1L)
        every { boardRepository.findByIdOrNull(1L) } returns null

        // When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.updateBoard(1L, boardUpdateRequest)
        }
        assertEquals("Board with ID 1 Not Found", ex.message)
    }

    @Test
    @DisplayName("Given duplicate board name When updateBoard Then throw IllegalArgumentException")
    fun given_DuplicateBoardName_when_UpdateBoard_then_ThrowIllegalArgumentException() {
        // Given
        val testBoard = BoardEntity(name = "Test Board", priority = 1, readableRole = RoleEntity(name = "User", level = 0), id = 1L)
        val boardFindWithName = BoardEntity(name = "Updated Board", priority = 2, readableRole = RoleEntity(name = "User", level = 0), id = 2L)
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 1, readableRoleId = 1L)
        every { boardRepository.findByIdOrNull(1L) } returns testBoard
        every { boardRepository.findByName("Updated Board") } returns boardFindWithName

        // When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.updateBoard(1L, boardUpdateRequest)
        }
        assertEquals("Board with name already exists", ex.message)
    }

    @Test
    @DisplayName("Given duplicate board priority When updateBoard Then throw IllegalArgumentException")
    fun given_DuplicateBoardPriority_when_UpdateBoard_then_ThrowIllegalArgumentException() {
        // Given
        val testBoard = BoardEntity(name = "Test Board", priority = 1, readableRole = RoleEntity(name = "User", level = 0), id = 1L)
        val boardFindWithPriority = BoardEntity(name = "Test Board 2", priority = 2, readableRole = RoleEntity(name = "User", level = 0), id = 2L)
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 2, readableRoleId = 1L)
        every { boardRepository.findByIdOrNull(1L) } returns testBoard
        every { boardRepository.findByName("Updated Board") } returns null
        every { boardRepository.findByPriority(2) } returns boardFindWithPriority

        // When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.updateBoard(1L, boardUpdateRequest)
        }
        assertEquals("Board with priority already exists", ex.message)
    }

    @Test
    @DisplayName("Given invalid readableRoleId When updateBoard Then throw NotFoundException")
    fun given_InvalidReadableRole_when_UpdateBoard_then_ThrowNotFoundException() {
        //Given
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 2, readableRoleId = 1L)
        val role = RoleEntity(name = "User", level = 0)
        val board = BoardEntity(name = "Old Board", priority = 1, readableRole = role)
        every { boardRepository.findByIdOrNull(1L) } returns board
        every { boardRepository.findByName("Updated Board") } returns null
        every { boardRepository.findByPriority(2) } returns null
        every { roleRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.updateBoard(1L, boardUpdateRequest)
        }
        assertEquals("Role with ID 1 Not Found", ex.message)
    }

    @Test
    @DisplayName("Given existing board When deleteBoard Then delete the board")
    fun given_ExistingBoard_when_DeleteBoard_then_DeleteTheBoard() {
        // Given
        val role = RoleEntity(name = "User", level = 0)
        val board = BoardEntity(name = "Board to Delete", priority = 1, readableRole = role)
        every { boardRepository.findByIdOrNull(1L) } returns board
        every { boardRepository.delete(any<BoardEntity>()) } returns Unit

        // When
        boardService.deleteBoard(1L)

        // Then
        verify(exactly = 1) { boardRepository.delete(any()) }
    }

    @Test
    @DisplayName("Given non-existing board When deleteBoard Then throw NotFoundException")
    fun given_NonExistingBoard_when_DeleteBoard_then_ThrowNotFoundException() {
        // Given
        every { boardRepository.findByIdOrNull(1L) } returns null

        // When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.deleteBoard(1L)
        }
        assertEquals("Board with ID 1 Not Found", ex.message)
    }
}
