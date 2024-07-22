package com.portfolio.community.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.portfolio.community.JwtTestUtils
import com.portfolio.community.dto.comment.CommentCreateRequest
import com.portfolio.community.dto.comment.CommentUpdateRequest
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.entity.*
import com.portfolio.community.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
class CommentControllerIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Value("\${jwt.secret}") private lateinit var secretKey: String
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var postRepository: PostRepository
    @Autowired private lateinit var commentRepository: CommentRepository

    private lateinit var level0Role: Role
    private lateinit var level1Role: Role
    private lateinit var level2Role: Role
    private lateinit var board: BoardEntity
    private lateinit var authorOfPost: UserEntity
    private lateinit var level0User: UserEntity
    private lateinit var level1User: UserEntity
    private lateinit var level2User: UserEntity
    private lateinit var staff: UserEntity
    private lateinit var post: PostEntity

    private lateinit var jwtLV0: String
    private lateinit var jwtLV1: String
    private lateinit var jwtLV2: String
    private lateinit var jwtStaff: String

    @BeforeEach
    fun setup() {
        boardRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        postRepository.deleteAll()
        commentRepository.deleteAll()

        level0Role = roleRepository.save(Role(name = "LV0", level = 0))
        level1Role = roleRepository.save(Role(name = "LV1", level = 1))
        level2Role = roleRepository.save(Role(name = "LV2", level = 2))
        board = boardRepository.save(BoardEntity(name = "Test Board", priority = 0, readableRole = level1Role))
        authorOfPost = userRepository.save(UserEntity(username = "Author Of Post", password = "password", role = level1Role))
        level0User = userRepository.save(UserEntity(username = "LV0 User", password = "password", role = level0Role))
        level1User = userRepository.save(UserEntity(username = "LV1 User", password = "password", role = level1Role))
        level2User = userRepository.save(UserEntity(username = "LV2 User", password = "password", role = level2Role))
        staff = userRepository.save(UserEntity(username = "Staff", password = "password", role = level0Role).apply { setStaff() })
        post = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = authorOfPost, board = board))

        jwtLV0 = JwtTestUtils.generateToken(Principal(level0User), 60 * 60 * 1000, secretKey)
        jwtLV1 = JwtTestUtils.generateToken(Principal(level1User), 60 * 60 * 1000, secretKey)
        jwtLV2 = JwtTestUtils.generateToken(Principal(level2User), 60 * 60 * 1000, secretKey)
        jwtStaff = JwtTestUtils.generateToken(Principal(staff), 60 * 60 * 1000, secretKey)
    }

    @Test
    fun given_CommentExists_when_GetCommentsByPost_then_ReturnOkAndCommentPage() {
        //Given
        commentRepository.saveAll((1..10).map { CommentEntity(content = "Comment $it", author = level1User, post = post) })

        //When
        val result = mockMvc.perform(get("/posts/{postId}/comments", post.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content[0].content").value("Comment 1"))
            .andExpect(jsonPath("$.content[1].content").value("Comment 2"))
    }

    @Test
    fun given_NoCommentExists_when_GetCommentsByPost_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(get("/posts/{postId}/comments", post.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_NotReadableUser_when_GetCommentsByPost_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(get("/posts/{postId}/comments", post.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_CommentExists_when_GetCommentsByAuthor_then_ReturnOkAndCommentPage() {
        //Given
        commentRepository.saveAll((1..10).map { CommentEntity(content = "Comment $it", author = level1User, post = post) })

        //When
        val result = mockMvc.perform(get("/users/{userId}/comments", level1User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content[0].content").value("Comment 1"))
            .andExpect(jsonPath("$.content[1].content").value("Comment 2"))
    }

    @Test
    fun given_NoCommentExists_when_GetCommentsByAuthor_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(get("/users/{userId}/comments", level1User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_ValidRequest_when_CreateComment_then_ReturnCreatedAndNewComment() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment", authorId = level1User.id)

        //When
        val result = mockMvc.perform(post("/posts/{postId}/comments", post.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentCreateRequest)))

        //Then
        result.andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").value("New Comment"))
            .andExpect(jsonPath("$.author.username").value("LV1 User"))
    }

    @Test
    fun given_InvalidPostId_when_CreateComment_then_ReturnForbidden() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment", authorId = level1User.id)

        //When
        val result = mockMvc.perform(post("/posts/{postId}/comments", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentCreateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidAuthorId_when_CreateComment_then_ReturnForbidden() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment", authorId = -1)

        //When
        val result = mockMvc.perform(post("/posts/{postId}/comments", post.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentCreateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_NotReadableUser_when_CreateComment_then_ReturnForbidden() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment", authorId = level0User.id)

        //When
        val result = mockMvc.perform(post("/posts/{postId}/comments", post.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentCreateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequestAndUserIsAuthorOfComment_when_UpdateComment_then_ReturnOkAndUpdatedComment() {
        //Given
        val testComment = commentRepository.save(CommentEntity(content = "Test Comment", author = level1User, post = post))
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")

        //When
        val result = mockMvc.perform(put("/comments/{commentId}", testComment.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentUpdateRequest)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").value("Updated Comment"))
    }

    @Test
    fun given_InvalidCommentId_when_UpdateComment_then_ReturnForbidden() {
        //Given
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")

        //When
        val result = mockMvc.perform(put("/comments/{commentId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAuthorOfComment_when_UpdateComment_then_ReturnForbidden() {
        //Given
        val testComment = commentRepository.save(CommentEntity(content = "Test Comment", author = level1User, post = post))
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")

        //When
        val result = mockMvc.perform(put("/comments/{commentId}", testComment.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(commentUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequestAndUserIsAuthorOfComment_when_DeleteComment_then_ReturnNoContent() {
        //Given
        val testComment = commentRepository.save(CommentEntity(content = "Test Comment", author = level1User, post = post))

        //When
        val result = mockMvc.perform(delete("/comments/{commentId}", testComment.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    @WithUserDetails("Staff")
    fun given_ValidRequestAndUserIsStaff_when_DeleteComment_then_ReturnNoContent() {
        //Given
        val testComment = commentRepository.save(CommentEntity(content = "Test Comment", author = level1User, post = post))

        //When
        val result = mockMvc.perform(delete("/comments/{commentId}", testComment.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    @WithUserDetails("LV2 User")
    fun given_UserIsNotAuthorOfComment_when_DeleteComment_then_ReturnForbidden() {
        //Given
        val testComment = commentRepository.save(CommentEntity(content = "Test Comment", author = level1User, post = post))

        //When
        val result = mockMvc.perform(delete("/comments/{commentId}", testComment.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV2"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

     @Test
     @WithUserDetails("LV1 User")
     fun given_InvalidCommentId_when_DeleteComment_then_ReturnForbidden() {
         //When
         val result = mockMvc.perform(delete("/comments/{commentId}", -1)
             .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1"))

         //Then
         result.andExpect(status().isForbidden)
             .andExpect(jsonPath("$.status").value(403))
             .andExpect(jsonPath("$.message").value("Access Denied"))
             .andExpect(jsonPath("$.details").exists())
     }
}