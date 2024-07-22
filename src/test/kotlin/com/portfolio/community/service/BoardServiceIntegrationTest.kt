package com.portfolio.community.service

import com.portfolio.community.dto.board.BoardBatchUpdateRequest
import com.portfolio.community.dto.board.BoardCreateRequest
import com.portfolio.community.dto.board.BoardUpdateRequest
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.PostEntity
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.PostRepository
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BoardServiceIntegrationTest {

    @Autowired private lateinit var boardService: BoardService

    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var postRepository: PostRepository

    private lateinit var defaultRole: Role

    @BeforeEach
    fun setup() {
        boardRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        defaultRole = roleRepository.save(Role(name = "User", level = 0))
    }

    @Test
    fun given_BoardExists_when_GetAllBoards_then_ReturnBoardList() {
        //Given
        val boards = (1..5).map { BoardEntity(name = "Board $it", priority = 5 - it, readableRole = defaultRole) }
        boardRepository.saveAll(boards)

        //When
        val result = boardService.getAllBoards()

        //Then
        assertEquals(5, result.size)
        assertEquals("Board 5", result[0].name)
        assertEquals(0, result[0].priority)
    }

    @Test
    fun given_ValidData_when_CreateBoard_then_CreateAndReturnNewBoard() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = defaultRole.id)

        //When
        val result = boardService.createBoard(boardCreateRequest)

        //Then
        assertEquals("New Board", result.name)
        assertEquals(0, result.priority)
    }

    @Test
    fun given_DuplicateName_when_CreateBoard_then_ThrowIllegalArgumentException() {
        //Given
        boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = defaultRole))
        val boardCreateRequest = BoardCreateRequest(name = "Test Board", priority = 1, readableRoleId = defaultRole.id)

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.createBoard(boardCreateRequest)
        }
        assertEquals("Board with name already exists", ex.message)
    }

    @Test
    fun given_DuplicatePriority_when_CreateBoard_then_ThrowIllegalArgumentException() {
        //Given
        boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = defaultRole))
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = defaultRole.id)

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.createBoard(boardCreateRequest)
        }
        assertEquals("Board with priority already exists", ex.message)
    }

    @Test
    fun given_InvalidRoleId_when_CreateBoard_then_ThrowNotFoundException() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = -1)

        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.createBoard(boardCreateRequest)
        }
        assertEquals("Role with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdateBoard_then_UpdateAndReturnUpdatedBoard() {
        //Given
        boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = defaultRole))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 1, readableRoleId = defaultRole.id)
        val targetBoardId = boardRepository.findByName("Test Board")!!.id

        //When
        val result = boardService.updateBoard(targetBoardId, boardUpdateRequest)

        //Then
        assertEquals("Updated Board", result.name)
        assertEquals(1, result.priority)
    }

    @Test
    fun given_InvalidBoardId_when_UpdateBoard_then_ThrowNotFoundException() {
        //Given
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 0, readableRoleId = defaultRole.id)

        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.updateBoard(-1, boardUpdateRequest)
        }
        assertEquals("Board with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_DuplicateName_when_UpdateBoard_then_ThrowIllegalArgumentException() {
        //Given
        boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = defaultRole))
        boardRepository.save(BoardEntity(name = "Updated Board", priority = 1, readableRole = defaultRole))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 0, readableRoleId = defaultRole.id)
        val targetBoardId = boardRepository.findByName("Test Board")!!.id

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.updateBoard(targetBoardId, boardUpdateRequest)
        }
        assertEquals("Board with name already exists", ex.message)
    }

    @Test
    fun given_DuplicatePriority_when_UpdateBoard_then_ThrowIllegalArgumentException() {
        //Given
        boardRepository.save(BoardEntity(name = "Test Board 1", priority = 0, readableRole = defaultRole))
        boardRepository.save(BoardEntity(name = "Test Board 2", priority = 1, readableRole = defaultRole))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 1, readableRoleId = defaultRole.id)
        val targetBoardId = boardRepository.findByName("Test Board 1")!!.id

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            boardService.updateBoard(targetBoardId, boardUpdateRequest)
        }
        assertEquals("Board with priority already exists", ex.message)
    }

    @Test
    fun given_InvalidRoleId_when_UpdateBoard_then_ThrowNotFoundException() {
        //Given
        boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = defaultRole))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 0, readableRoleId = -1)
        val targetBoardId = boardRepository.findByName("Test Board")!!.id

        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.updateBoard(targetBoardId, boardUpdateRequest)
        }
        assertEquals("Role with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteBoard_then_DeleteBoard() {
        //Given
        boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = defaultRole))
        val targetBoardId = boardRepository.findByName("Test Board")!!.id

        //When
        boardService.deleteBoard(targetBoardId)

        //Then
        val result = boardService.getAllBoards()
        assertEquals(result.size, 0)
    }

    @Test
    fun given_InvalidBoardId_when_DeleteBoard_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.deleteBoard(-1)
        }
        assertEquals("Board with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteBoardAndMovePosts_then_DeleteBoardAndMovePosts() {
        //Given
        val sourceBoard = boardRepository.save(BoardEntity(name = "Source Board", priority = 0, readableRole = defaultRole))
        val targetBoard = boardRepository.save(BoardEntity(name = "Target Board", priority = 1, readableRole = defaultRole))
        val author = userRepository.save(UserEntity(username = "Author", password = "password", role = defaultRole))
        (1..5).map { postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = sourceBoard)) }

        //When
        val result = boardService.deleteBoardAndMovePosts(sourceBoard.id, targetBoard.id)

        //Then
        assertEquals(5, result.postCount)
        assertEquals("Target Board", result.name)
    }

    @Test
    fun given_InvalidSourceId_when_DeleteBoardAndMovePosts_then_ThrowNotFoundException() {
        //Given
        val targetBoard = boardRepository.save(BoardEntity(name = "Target Board", priority = 1, readableRole = defaultRole))

        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.deleteBoardAndMovePosts(-1, targetBoard.id)
        }
        assertEquals("Board with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidTargetId_when_DeleteBoardAndMovePosts_then_ThrowNotFoundException() {
        //Given
        val sourceBoard = boardRepository.save(BoardEntity(name = "Source Board", priority = 0, readableRole = defaultRole))
        val author = userRepository.save(UserEntity(username = "Author", password = "password", role = defaultRole))
        (1..5).map { postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = sourceBoard)) }

        //When & Then
        val ex = assertThrows<NotFoundException> {
            boardService.deleteBoardAndMovePosts(sourceBoard.id, -1)
        }
        assertEquals("Board with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_BatchUpdateBoard_then_ReturnBoardList() {
        //Given
        //priority 5를 삭제하고 0~4를 1씩 올린 뒤 새로운 0을 생성, 그리고 6은 삭제한 뒤 0에 move
        val boards = boardRepository.saveAll((1..7).map { BoardEntity(name = "Board NO.$it", priority = it -1, readableRole = defaultRole) })
        val batchUpdateRequest = BoardBatchUpdateRequest(
            updates = (1..5).map { Pair(boards[it-1].id, BoardUpdateRequest(name = "Updated Board NO.$it", priority = it, readableRoleId = defaultRole.id)) },
            creates = listOf(BoardCreateRequest(name = "New Board NO.8", priority = 0, readableRoleId = defaultRole.id)),
            deletes = listOf(boards[5].id),
            moves = listOf(Pair(boards[6].id, boards[0].id))
        )
        val author = userRepository.save(UserEntity(username = "Author", password = "password", role = defaultRole))
        (1..5).map { postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = boards[6])) }

        //When
        val result = boardService.batchUpdateBoard(batchUpdateRequest)

        //Then
        assertEquals("New Board NO.8", result[0].name)
        assertEquals(0, result[0].priority)
        assertEquals("Updated Board NO.1", result[1].name)
        assertEquals(1, result[1].priority)
        assertEquals(5, result[1].postCount)
        assertEquals("Updated Board NO.2", result[2].name)
        assertEquals(2, result[2].priority)
    }

}