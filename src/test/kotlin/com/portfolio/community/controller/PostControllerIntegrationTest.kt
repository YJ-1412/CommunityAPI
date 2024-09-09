package com.portfolio.community.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.portfolio.community.JwtTestUtils
import com.portfolio.community.dto.post.PostCreateRequest
import com.portfolio.community.dto.post.PostUpdateRequest
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.entity.*
import com.portfolio.community.repository.*
import com.portfolio.community.service.PostService
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
class PostControllerIntegrationTest {

    @Autowired private lateinit var postService: PostService
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
    private lateinit var authorOfComment: UserEntity
    private lateinit var level0User: UserEntity
    private lateinit var level1User: UserEntity
    private lateinit var level2User: UserEntity
    private lateinit var staff: UserEntity

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
        board = boardRepository.save(BoardEntity(name = "Board", priority = 0, readableRole = level1Role))
        authorOfComment = userRepository.save(UserEntity(username = "Author Of Comment", password = "password", role = level1Role))
        level0User = userRepository.save(UserEntity(username = "LV0 User", password = "password", role = level0Role))
        level1User = userRepository.save(UserEntity(username = "LV1 User", password = "password", role = level1Role))
        level2User = userRepository.save(UserEntity(username = "LV2 User", password = "password", role = level2Role))
        staff = userRepository.save(UserEntity(username = "Staff", password = "password", role = level0Role).apply { setStaff() })

        jwtLV0 = JwtTestUtils.generateToken(Principal(level0User), 60 * 60 * 1000, secretKey)
        jwtLV1 = JwtTestUtils.generateToken(Principal(level1User), 60 * 60 * 1000, secretKey)
        jwtLV2 = JwtTestUtils.generateToken(Principal(level2User), 60 * 60 * 1000, secretKey)
        jwtStaff = JwtTestUtils.generateToken(Principal(staff), 60 * 60 * 1000, secretKey)
    }

    @Test
    fun given_PostExists_when_GetAllPostInfos_then_ReturnOkAndPostPage() {
        //Given
        postRepository.saveAll((1..10).map { PostEntity(title = "Post $it", content = "Content $it", author = level1User, board = board) })

        //When
        val result = mockMvc.perform(get("/posts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))


        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content[0].title").value("Post 10"))
            .andExpect(jsonPath("$.content[1].title").value("Post 9"))
    }

    @Test
    fun given_NoPostExists_when_GetAllPostInfos_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(get("/posts")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_PostExists_when_GetPostInfosByBoard_then_ReturnOkAndPostPage() {
        //Given
        postRepository.saveAll((1..10).map { PostEntity(title = "Post $it", content = "Content $it", author = level1User, board = board) })

        //When
        val result = mockMvc.perform(get("/boards/{boardId}/posts", board.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content[0].title").value("Post 10"))
            .andExpect(jsonPath("$.content[1].title").value("Post 9"))
    }

    @Test
    fun given_NoPostExists_when_GetPostInfosByBoard_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(get("/boards/{boardId}/posts", board.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_PostExists_when_GetPostInfosByAuthor_then_ReturnOkAndPostPage() {
        //Given
        postRepository.saveAll((1..10).map { PostEntity(title = "Post $it", content = "Content $it", author = level1User, board = board) })

        //When
        val result = mockMvc.perform(get("/users/{userId}/posts", level1User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content[0].title").value("Post 10"))
            .andExpect(jsonPath("$.content[1].title").value("Post 9"))
    }

    @Test
    fun given_NoPostsExists_when_GetPostInfosByAuthor_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(get("/users/{userId}/posts", level1User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_PostExists_when_GetLikedPostsByUser_then_ReturnOkAndPostPage() {
        //Given
        val posts = postRepository.saveAll((1..10).map { PostEntity(title = "Post $it", content = "Content $it", author = level1User, board = board) })
        posts.forEach { postService.likePost(level2User.id, it.id) }

        //When
        val result = mockMvc.perform(get("/users/{userId}/liked-posts", level2User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.content[0].title").value("Post 10"))
            .andExpect(jsonPath("$.content[1].title").value("Post 9"))
    }

    @Test
    fun given_NoPostExists_when_GetLikedPostsByUser_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(get("/users/{userId}/liked-posts", level2User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .param("page", "0")
            .param("size", "10"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_ValidRequest_when_LikePost_then_ReturnOkAndPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(post("/posts/{postId}/liked-users", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV2"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.likeCount").value(1))
    }

    @Test
    fun given_InvalidPostId_when_LikePost_then_ReturnForbidden() {//When
        val result = mockMvc.perform(post("/posts/{postId}/liked-users", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV2"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsAuthorOfPost_when_LikePost_then_ReturnConflict() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(post("/posts/{postId}/liked-users", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1"))

        //Then
        result.andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("Author of post can not like post"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserCanNotReadPost_when_LikePost_then_ReturnForbidden() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(post("/posts/{postId}/liked-users", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_UnlikePost_then_ReturnOkAndPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))
        postService.likePost(level2User.id, testPost.id)

        //When
        val result = mockMvc.perform(delete("/posts/{postId}/liked-users", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV2"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.likeCount").value(0))
    }

    @Test
    fun given_UserIsNotLiked_when_UnlikePost_then_ReturnConflict() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(delete("/posts/{postId}/liked-users", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV2"))

        //Then
        result.andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("User did not like this post"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidPostId_when_UnlikePost_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(delete("/posts/{postId}/liked-users", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV2"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserCanNotReadPost_when_UnlikePost_then_ReturnForbidden() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(delete("/posts/{postId}/liked-users", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_GetPost_then_ReturnOkAndPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(get("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.content").value("Test Content"))
            .andExpect(jsonPath("$.viewCount").value(1))
    }

    @Test
    fun given_InvalidPostId_when_GetPost_then_ReturnForbidden() {
        //Given
        val result = mockMvc.perform(get("/posts/{postId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserCanNotReadPost_when_GetPost_then_ReturnForbidden() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(get("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_CreatePost_then_ReturnCreatedAndNewPost() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "New Content")

        //When
        val result = mockMvc.perform(post("/boards/{boardId}/posts", board.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postCreateRequest)))

        //Then
        result.andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("New Post"))
            .andExpect(jsonPath("$.content").value("New Content"))
            .andExpect(jsonPath("$.author.username").value("LV1 User"))
            .andExpect(jsonPath("$.viewCount").value(0))
    }

    @Test
    fun given_InvalidBoardId_when_CreatePost_then_ReturnForbidden() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "New Content")

        //When
        val result = mockMvc.perform(post("/boards/{boardId}/posts", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postCreateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserCanNotCreatePost_when_CreatePost_then_ReturnForbidden() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "New Content")

        //When
        val result = mockMvc.perform(post("/boards/{boardId}/posts", board.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postCreateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_UpdatePost_then_ReturnOkAndUpdatedPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = board.id)

        //When
        val result = mockMvc.perform(put("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postUpdateRequest)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("Updated Post"))
            .andExpect(jsonPath("$.content").value("Updated Content"))
    }

    @Test
    fun given_InvalidPostId_when_UpdatePost_then_ReturnForbidden() {
        //Given
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = board.id)

        //When
        val result = mockMvc.perform(put("/posts/{postId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidBoardId_when_UpdatePost_then_ReturnForbidden() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = -1)

        //When
        val result = mockMvc.perform(put("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UnreadableBoardId_when_UpdatePost_then_ReturnForbidden() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))
        val unreadableBoard = boardRepository.save(BoardEntity(name = "Unreadable Board", priority = 1, readableRole = level2Role))
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = unreadableBoard.id)

        //When
        val result = mockMvc.perform(put("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAuthorOfPost_when_UpdatePost_then_ReturnForbidden() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = -1)

        //When
        val result = mockMvc.perform(put("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(postUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequestAndUserIsAuthorOfPost_when_DeletePost_then_ReturnNoContent() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(delete("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_ValidRequestAndUserIsAdminOfStaff_when_DeletePost_then_ReturnNoContent() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(delete("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_InvalidPostId_when_DeletePost_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(delete("/posts/{postId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAuthorAndNotStaff_when_DeletePost_then_ReturnForbidden() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Test Content", author = level1User, board = board))

        //When
        val result = mockMvc.perform(delete("/posts/{postId}", testPost.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV2"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

}