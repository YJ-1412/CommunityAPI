package com.portfolio.community.service

import com.portfolio.community.dto.post.PostCreateRequest
import com.portfolio.community.dto.post.PostUpdateRequest
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.PostEntity
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class PostServiceIntegrationTest {

    @Autowired private lateinit var postService: PostService
    @Autowired private lateinit var postRepository: PostRepository
    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository

    private lateinit var defaultRole: Role
    private lateinit var board: BoardEntity
    private lateinit var author: UserEntity

    @BeforeEach
    fun setUp() {
        postRepository.deleteAll()
        boardRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()

        defaultRole = roleRepository.save(Role("User", level = 0))
        board = boardRepository.save(BoardEntity(name = "Board", priority = 0, readableRole = defaultRole))
        author = userRepository.save(UserEntity(username = "Author", password = "password", role = defaultRole))
    }

    @Test
    fun given_PostExists_when_GetAllPostInfos_then_ReturnPostInfoPage() {
        //Given
        (1..10).forEach() {
            postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = board))
            Thread.sleep(1)
        }

        //When
        val result = postService.getAllPostInfos(0, 10)

        //Then
        assertEquals(10, result.size)
        assertEquals("Post 10", result.content[0].title)
    }

    @Test
    fun given_PostExists_when_GetPostInfosByBoardId_then_ReturnPostInfoPage() {
        //Given
        (1..10).forEach() {
            postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = board))
            Thread.sleep(1)
        }

        //When
        val result = postService.getPostInfosByBoardId(board.id, 0, 10)

        //Then
        assertEquals(10, result.size)
        assertEquals("Post 10", result.content[0].title)
    }

    @Test
    fun given_InvalidBoardId_when_GetPostInfosByBoardId_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.getPostInfosByBoardId(-1, 0, 10)
        }
        assertEquals("Board with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_PostExists_when_GetPostInfosByAuthorId_then_ReturnPostInfo() {
        //Given
        (1..10).forEach() {
            postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = board))
            Thread.sleep(1)
        }
        //When
        val result = postService.getPostInfosByAuthorId(author.id, 0, 10)

        //Then
        assertEquals(10, result.size)
        assertEquals("Post 10", result.content[0].title)
    }

    @Test
    fun given_InvalidAuthorId_when_GetPostInfosByAuthorId_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.getPostInfosByAuthorId(-1, 0, 10)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_PostExists_when_GetPostInfosByLikedUserId_then_ReturnPostInfo() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        (1..10).forEach() {
            postRepository.save(PostEntity(title = "Post $it", content = "Content $it", author = author, board = board))
            Thread.sleep(1)
        }
        val posts = postRepository.findAllByOrderByIdDesc(PageRequest.of(0, 10)).content
        posts.map {
            postService.likePost(testUser.id, it.id)
            Thread.sleep(1)
        }

        //When
        val result = postService.getPostInfosByLikedUserId(testUser.id, 0, 10)

        //Then
        assertEquals(10, result.size)
        assertEquals("Post 1", result.content[0].title)
    }

    @Test
    fun given_InvalidLikedUserId_when_GetPostInfosByLikedUserId_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.getPostInfosByLikedUserId(-1, 0, 10)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_LikePost_then_ReturnPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When
        val result = postService.likePost(testUser.id, testPost.id)

        //Then
        assertEquals(1, result.likeCount)
    }

    @Test
    fun given_UserAlreadyLikedPost_when_LikePost_then_ThrowIllegalStateException() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        postService.likePost(testUser.id, testPost.id)

        //When & Then
        val ex = assertThrows<IllegalStateException> {
            postService.likePost(testUser.id, testPost.id)
        }
        assertEquals("User already liked this post", ex.message)
    }

    @Test
    fun given_InvalidUserId_when_LikePost_then_ThrowNotFoundException() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.likePost(-1, testPost.id)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidPostId_when_LikePost_then_ThrowNotFoundException() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.likePost(testUser.id, -1)
        }
        assertEquals("Post with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UnlikePost_then_ReturnPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        postService.likePost(testUser.id, testPost.id)

        //When
        val result = postService.unlikePost(testUser.id, testPost.id)

        //Then
        assertEquals(0, result.likeCount)
    }

    @Test
    fun given_InvalidUserId_when_UnlikePost_then_ThrowNotFoundException() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        postService.likePost(testUser.id, testPost.id)

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.unlikePost(-1, testPost.id)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidPostId_when_UnlikePost_then_ThrowNotFoundException() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        postService.likePost(testUser.id, testPost.id)

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.unlikePost(testUser.id, -1)
        }
        assertEquals("Post with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_UserNotLikedPost_when_UnlikePost_then_ThrowIllegalStateException() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When & Then
        val ex = assertThrows<IllegalStateException> {
            postService.unlikePost(testUser.id, testPost.id)
        }
        assertEquals("User did not like this post", ex.message)
    }

    @Test
    fun given_ValidData_when_IncreaseViewCount_then_ReturnPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))

        //When
        val result = postService.increaseViewCount(testPost.id)

        //Then
        assertEquals(1, result.viewCount)
    }

    @Test
    fun given_InvalidPostId_when_IncreaseViewCount_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.increaseViewCount(-1)
        }
        assertEquals("Post with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_CreatePost_then_CreateAndReturnNewPost() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "Content")

        //When
        val result = postService.createPost(board.id, author.id, postCreateRequest)

        //Then
        assertEquals("New Post", result.title)
    }

    @Test
    fun given_InvalidBoardId_when_CreatePost_then_ThrowNotFoundException() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "Content")

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.createPost(-1, author.id, postCreateRequest)
        }
        assertEquals("Board with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidAuthorId_when_CreatePost_then_ThrowNotFoundException() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "Content")

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.createPost(board.id, -1, postCreateRequest)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdatePost_then_UpdateAndReturnUpdatedPost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val testBoard = boardRepository.save(BoardEntity(name = "Test Board", priority = 1, readableRole = defaultRole))
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = testBoard.id)

        //When
        val result = postService.updatePost(testPost.id, postUpdateRequest)

        //Then
        assertEquals("Updated Post", result.title)
        assertEquals("Updated Content", result.content)
        assertEquals("Test Board", result.board.name)
    }

    @Test
    fun given_InvalidPostId_when_UpdatePost_then_ThrowNotFoundException() {
        //Given
        val testBoard = boardRepository.save(BoardEntity(name = "Test Board", priority = 1, readableRole = defaultRole))
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = testBoard.id)

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.updatePost(-1, postUpdateRequest)
        }
        assertEquals("Post with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidBoardId_when_UpdatePost_then_ThrowNotFoundException() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = -1)

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.updatePost(testPost.id, postUpdateRequest)
        }
        assertEquals("Board with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeletePost_then_DeletePost() {
        //Given
        val testPost = postRepository.save(PostEntity(title = "Test Post", content = "Content", author = author, board = board))

        //When
        postService.deletePost(testPost.id)

        //Then
        val ex = assertThrows<NotFoundException> {
            postService.increaseViewCount(testPost.id)
        }
        assertEquals("Post with ID ${testPost.id} Not Found", ex.message)
    }

    @Test
    fun given_InvalidPostId_when_DeletePost_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.deletePost(-1)
        }
        assertEquals("Post with ID -1 Not Found", ex.message)
    }
}