package com.portfolio.community.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.portfolio.community.JwtTestUtils
import com.portfolio.community.dto.board.*
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.PostEntity
import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.PostRepository
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BoardControllerIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Value("\${jwt.secret}") private lateinit var secretKey: String
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var postRepository: PostRepository

    private lateinit var level0Role: RoleEntity
    private lateinit var level1Role: RoleEntity
    private lateinit var regular: UserEntity
    private lateinit var admin: UserEntity

    private lateinit var jwtTokenOfRegular: String
    private lateinit var jwtTokenOfAdmin: String

    @BeforeEach
    fun setup() {
        boardRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()

        level0Role = roleRepository.save(RoleEntity(name = "LV0", level = 0))
        level1Role = roleRepository.save(RoleEntity(name = "LV1", level = 1))

        admin = userRepository.save(UserEntity(username = "Admin", password = "00000000", role = level0Role).apply { setAdmin() })
        regular = userRepository.save(UserEntity(username = "User", password = "00000000", role = level0Role))

        jwtTokenOfAdmin = JwtTestUtils.generateToken(Principal(admin), 60 * 60 * 1000, secretKey)
        jwtTokenOfRegular = JwtTestUtils.generateToken(Principal(regular), 60 * 60 * 1000, secretKey)
    }

    @Test
    fun given_BoardsExists_when_GetAllBoards_then_ReturnOkAndBoardList() {
        //Given
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = level0Role) })

        //When
        val result = mockMvc.perform(get("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfRegular"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].size()").value(5))
            .andExpect(jsonPath("$[0].name").value("Board 1"))
            .andExpect(jsonPath("$[0].priority").value(0))
            .andExpect(jsonPath("$[0].readableRole.name").value("LV0"))
    }

    @Test
    fun given_NoBoards_when_GetAllBoards_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(get("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfRegular"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_Unauthenticated_when_GetAllBoards_then_ReturnOk() {
        //Given
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = level0Role) })

        //When
        val result = mockMvc.perform(get("/boards"))

        //Then
        result.andExpect(status().isOk)
    }

    @Test
    fun given_ValidRequest_when_CreateBoard_then_ReturnCreatedAndNewBoard() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        val newBoard = boardRepository.findByName("New Board")!!
        result.andExpect(header().string("Location", "/boards/${newBoard.id}"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("New Board"))
            .andExpect(jsonPath("$.priority").value(0))
            .andExpect(jsonPath("$.readableRole.name").value("LV0"))
    }

    @Test
    fun given_BlankName_when_CreateBoard_then_ReturnBadRequest() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "", priority = 0, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Name must not be blank"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicateName_when_CreateBoard_then_ReturnBadRequest() {
        //Given
        boardRepository.save(BoardEntity("New Board", priority = 0, readableRole = level0Role))
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 1, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Board with name already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_NullPriority_when_CreateBoard_then_ReturnBadRequest() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = null, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Priority must not be null"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidPriority_when_CreateBoard_then_ReturnBadRequest() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = -1, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Priority must be greater than or equal to 0"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicatePriority_when_CreateBoard_then_ReturnBadRequest() {
        //Given
        boardRepository.save(BoardEntity(name = "New Board 1", priority = 0, readableRole = level0Role))
        val boardCreateRequest = BoardCreateRequest(name = "New Board 2", priority = 0, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Board with priority already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_NullReadableRoleId_when_CreateBoard_then_ReturnBadRequest() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = null)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("ReadableRoleId must not be null"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidReadableRoleId_when_CreateBoard_then_ReturnNotFound() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = -1L)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Role with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_Unauthorized_when_CreateBoard_then_ReturnUnauthorized() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Unauthorized"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_UpdateBoard_then_BoardIsUpdated() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board", priority = 0, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 1, readableRoleId = level1Role.id)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Updated Board"))
            .andExpect(jsonPath("$.priority").value(1))
            .andExpect(jsonPath("$.readableRole.name").value("LV1"))
    }

    @Test
    fun given_InvalidBoardId_when_UpdateBoard_then_ReturnNotFound() {
        //Given
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 0, readableRoleId = level1Role.id)

        //When
        val result = mockMvc.perform(put("/boards/-1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Board with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_BlankName_when_UpdateBoard_then_ReturnBadRequest() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board", priority = 0, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "", priority = 1, readableRoleId = level1Role.id)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Name must not be blank"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicateName_when_UpdateBoard_then_ReturnBadRequest() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board 1", priority = 0, readableRole = level0Role))
        boardRepository.save(BoardEntity("Test Board 2", priority = 1, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Test Board 2", priority = 3, readableRoleId = level1Role.id)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Board with name already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_NullPriority_when_UpdateBoard_then_ReturnBadRequest() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board", priority = 0, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = null, readableRoleId = level1Role.id)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Priority must not be null"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidPriority_when_UpdateBoard_then_ReturnBadRequest() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board", priority = 0, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = -1, readableRoleId = level1Role.id)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Priority must be greater than or equal to 0"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicatePriority_when_UpdateBoard_then_ReturnBadRequest() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board 1", priority = 0, readableRole = level0Role))
        boardRepository.save(BoardEntity("Test Board 2", priority = 1, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Test Board 3", priority = 1, readableRoleId = level1Role.id)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Board with priority already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun givenNullReadableRoleId_whenUpdateBoard_thenReturnBadRequest() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board", priority = 0, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 1, readableRoleId = null)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("ReadableRoleId must not be null"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidReadableRoleId_when_UpdateBoard_then_ReturnNotFound() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board", priority = 0, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 1, readableRoleId = -1)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Role with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_DeleteBoard_then_BoardIsDeleted() {
        //Given
        val testBoard = boardRepository.save(BoardEntity("Test Board", priority = 0, readableRole = level0Role))

        //When
        val result = mockMvc.perform(delete("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_InvalidBoardId_when_DeleteBoard_then_ReturnNotFound() {
        //When
        val result = mockMvc.perform(delete("/boards/-1")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Board with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_DeleteBoardAndMovePosts_then_ReturnOkAndTargetBoard() {
        //Given
        val sourceBoard = boardRepository.save(BoardEntity("Source Board", priority = 0, readableRole = level0Role))
        val author = userRepository.save(UserEntity(username = "Author", password = "password", role = level0Role))
        postRepository.saveAll((1..5).map { PostEntity(title = "Post $it", content = "Content $it", author = author, board = sourceBoard) })
        val targetBoard = boardRepository.save(BoardEntity("Target Board", priority = 1, readableRole = level1Role))

        //When
        val result = mockMvc.perform(delete("/boards/{sourceBoardId}/posts/transfer/{targetBoardId}", sourceBoard.id, targetBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Target Board"))
            .andExpect(jsonPath("$.priority").value(1))
            .andExpect(jsonPath("$.readableRole.name").value("LV1"))
            .andExpect(jsonPath("$.postCount").value(5))
    }

    @Test
    fun given_InvalidSourceBoardId_when_DeleteBoardAndMovePosts_then_ReturnNotFound() {
        //Given
        val targetBoard = boardRepository.save(BoardEntity("Target Board", priority = 1, readableRole = level1Role))

        //When
        val result = mockMvc.perform(delete("/boards/{sourceBoardId}/posts/transfer/{targetBoardId}", -1, targetBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Board with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidTargetBoardId_when_DeleteBoardAndMovePosts_then_ReturnNotFound() {
        //Given
        val sourceBoard = boardRepository.save(BoardEntity("Source Board", priority = 0, readableRole = level0Role))
        val author = userRepository.save(UserEntity(username = "Author", password = "password", role = level0Role))
        postRepository.saveAll((1..5).map { PostEntity(title = "Post $it", content = "Content $it", author = author, board = sourceBoard) })

        //When
        val result = mockMvc.perform(delete("/boards/{sourceBoardId}/posts/transfer/{targetBoardId}", sourceBoard.id, -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Board with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_RegularUser_when_CreateBoard_then_ReturnForbidden() {
        //Given
        val boardCreateRequest = BoardCreateRequest(name = "New Board", priority = 0, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(post("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfRegular")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardCreateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_RegularUser_when_UpdateBoard_then_ReturnForbidden() {
        //When
        val testBoard = boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = level0Role))
        val boardUpdateRequest = BoardUpdateRequest(name = "Updated Board", priority = 0, readableRoleId = level0Role.id)

        //When
        val result = mockMvc.perform(put("/boards/{boardId}", testBoard.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfRegular")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(boardUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_BatchUpdateBoard_then_ReturnOkAndBoardList() {
        //Given
        //priority 5를 삭제하고 0~4를 1씩 올린 뒤 새로운 0을 생성, 그리고 6은 삭제한 뒤 0에 move
        val boards = boardRepository.saveAll((1..7).map { BoardEntity(name = "Board NO.$it", priority = it -1, readableRole = level0Role) })
        val batchUpdateRequest = BoardBatchUpdateRequest(
            updates = (1..5).map { Pair(boards[it-1].id, BoardUpdateRequest(name = "Updated Board NO.$it", priority = it, readableRoleId = level0Role.id)) },
            creates = listOf(BoardCreateRequest(name = "New Board NO.8", priority = 0, readableRoleId = level0Role.id)),
            deletes = listOf(boards[5].id),
            moves = listOf(Pair(boards[6].id, boards[0].id))
        )
        val author = userRepository.save(UserEntity(username = "Author", password = "password", role = level0Role))
        (1..5).map { postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = boards[6])) }

        //When
        val result = mockMvc.perform(put("/boards")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtTokenOfAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(batchUpdateRequest)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("New Board NO.8"))
            .andExpect(jsonPath("$[0].priority").value(0))
            .andExpect(jsonPath("$[0].postCount").value(0))
            .andExpect(jsonPath("$[1].name").value("Updated Board NO.1"))
            .andExpect(jsonPath("$[1].priority").value(1))
            .andExpect(jsonPath("$[1].postCount").value(5))
    }
}
