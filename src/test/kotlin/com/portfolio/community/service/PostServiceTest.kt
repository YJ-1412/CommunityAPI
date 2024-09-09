package com.portfolio.community.service

import com.portfolio.community.dto.post.PostCreateRequest
import com.portfolio.community.dto.post.PostUpdateRequest
import com.portfolio.community.entity.*
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.LikeRepository
import com.portfolio.community.repository.PostRepository
import com.portfolio.community.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

class PostServiceTest {

    private lateinit var postService: PostService
    private lateinit var postRepository: PostRepository
    private lateinit var boardRepository: BoardRepository
    private lateinit var userRepository: UserRepository
    private lateinit var likeRepository: LikeRepository

    private lateinit var author: UserEntity
    private lateinit var board: BoardEntity
    private lateinit var role: Role

    @BeforeEach
    fun setUp() {
        postRepository = mockk()
        boardRepository = mockk()
        userRepository = mockk()
        likeRepository = mockk()
        postService = PostService(postRepository, boardRepository, userRepository, likeRepository)

        role = Role(name = "User", level = 0)
        author = UserEntity(username = "Author", password = "password", role = role, id = 1L)
        board = BoardEntity(name = "Board 1", priority = 1, readableRole = role, id = 1L)
    }

    @Test
    fun given_PostExists_when_GetAllPostInfos_then_ReturnAllPostInfoPage() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val posts = (1..10).map { PostEntity(title = "Post $it", content = "Content $it", author = author, board = board) }
        val postPage = PageImpl(posts, pageable, posts.size.toLong())
        every { postRepository.findAllByOrderByIdDesc(pageable) } returns postPage

        //When
        val result = postService.getAllPostInfos(0, 10)

        //Then
        assertEquals(10, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals("Post 1", result.content[0].title)

    }

    @Test
    fun given_PostAndBoardAndAuthorExists_when_GetPostInfosByBoardId_then_ReturnPostInfoPageOnBoard() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val posts = (1..10).map { PostEntity(title = "Post $it", content = "Content $it", author = author, board = board) }
        val postPage = PageImpl(posts, pageable, posts.size.toLong())
        every { boardRepository.existsById(1L) } returns true
        every { postRepository.findByBoardIdOrderByIdDesc(1L, pageable) } returns postPage

        //When
        val result = postService.getPostInfosByBoardId(1L, 0, 10)

        //Then
        assertEquals(10, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals("Post 1", result.content[0].title)
    }

    @Test
    fun given_InvalidBoardId_when_GetPostInfosByBoardId_then_ThrowNotFoundException() {
        //Given
        every { boardRepository.existsById(1L) } returns false

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.getPostInfosByBoardId(1L, 0, 10)
        }
        assertEquals("Board with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_PostAndBoardAndAuthorExists_when_GetPostInfosByAuthorId_then_ReturnPostInfoPageByAuthor() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val posts = (1..10).map { PostEntity(title = "Post $it", content = "Content $it", author = author, board = board) }
        val postPage = PageImpl(posts, pageable, posts.size.toLong())
        every { userRepository.existsById(1L) } returns true
        every { postRepository.findByAuthorIdOrderByIdDesc(1L, pageable) } returns postPage

        //When
        val result = postService.getPostInfosByAuthorId(1L, 0, 10)

        //Then
        assertEquals(10, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals("Post 1", result.content[0].title)
    }

    @Test
    fun given_InvalidAuthorId_when_GetPostInfosByAuthorId_then_ThrowNotFoundException() {
        //Given
        every { userRepository.existsById(1L) } returns false

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.getPostInfosByAuthorId(1L, 0, 10)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_GetPostInfosByLikedUserId_then_ReturnPostInfoPage() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val testUser = UserEntity(username = "Test User", password = "password", role = role, id = 1L)
        val likes = (1..10).map { UserLikePost(user = testUser, post = PostEntity(title = "Post $it", content = "Content $it", author = author, board = board, id = it.toLong())) }
        val likePage = PageImpl(likes, pageable, likes.size.toLong())
        every { userRepository.existsById(1L) } returns true
        every { likeRepository.findAllByUserIdOrderByIdDesc(1L, pageable) } returns likePage

        //When
        val result = postService.getPostInfosByLikedUserId(1L, 0, 10)

        //Then
        assertEquals(10, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals("Post 1", result.content[0].title)
    }

    @Test
    fun given_InvalidUserId_when_GetPostInfosByLikedUserId_then_ThrowNotFoundException() {
        //Given
        every { userRepository.existsById(1L) } returns false

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.getPostInfosByLikedUserId(1L, 0, 10)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_IncreaseViewCount_then_IncreaseViewCount() {
        //Given
        val post = PostEntity(title = "Post 1", content = "Content 1", author = author, board = board, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns post

        //When
        val result = postService.increaseViewCount(1L)

        //Then
        assertEquals(1, result.viewCount)
    }

    @Test
    fun given_InvalidPostId_when_IncreaseViewCount_then_ThrowNotFoundException() {
        //Given
        every { postRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.increaseViewCount(1L)
        }
        assertEquals("Post with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_CreatePost_then_CreateAndReturnNewPost() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "Content")
        val newPost = PostEntity(title = "New Post", content = "Content", author = author, board = board)
        every { userRepository.findByIdOrNull(1L) } returns author
        every { boardRepository.findByIdOrNull(1L) } returns board
        every { postRepository.save(any()) } returns newPost

        //When
        val result = postService.createPost(1L, 1L, postCreateRequest)

        //Then
        assertEquals("New Post", result.title)
        assertEquals("Content", result.content)
        verify(exactly = 1) { postRepository.save(any()) }
    }

    @Test
    fun given_InvalidAuthorId_when_CreatePost_then_ThrowNotFoundException() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "Content")
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.createPost(1L, 1L, postCreateRequest)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidBoardId_when_CreatePost_then_ThrowNotFoundException() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "Content")
        every { userRepository.findByIdOrNull(1L) } returns author
        every { boardRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.createPost(1L, 1L, postCreateRequest)
        }
        assertEquals("Board with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdatePost_then_UpdateAndReturnUpdatedPost() {
        //Given
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = 2L)
        val oldPost = PostEntity(title = "Old Post", content = "Old Content", author = author, board = board, id = 1L)
        val testBoard = BoardEntity(name = "Board 2", priority = 2, readableRole = role)
        every { postRepository.findByIdOrNull(1L) } returns oldPost
        every { boardRepository.findByIdOrNull(2L) } returns testBoard

        //When
        val result = postService.updatePost(1L, postUpdateRequest)

        //Then
        assertEquals("Updated Post", result.title)
        assertEquals("Updated Content", result.content)
        assertEquals("Board 2", result.board.name)
    }

    @Test
    fun given_InvalidPostId_when_UpdatePost_then_ThrowNotFoundException() {
        //Given
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = 2L)
        every { postRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.updatePost(1L, postUpdateRequest)
        }
        assertEquals("Post with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidBoardId_when_UpdatePost_then_ThrowNotFoundException() {
        //Given
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = 2L)
        val oldPost = PostEntity(title = "Old Post", content = "Old Content", author = author, board = board, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns oldPost
        every { boardRepository.findByIdOrNull(2L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.updatePost(1L, postUpdateRequest)
        }
        assertEquals("Board with ID 2 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeletePost_then_DeletePost() {
        //Given
        val testPost = PostEntity(title = "Test Post", content = "Content", author = author, board = board, id = 1L)
        every { postRepository.findByIdOrNull(1L) } returns testPost
        every { postRepository.delete(any()) } returns Unit

        //When & Then
        postService.deletePost(1L)
        verify(exactly = 1) { postRepository.delete(any()) }
    }

    @Test
    fun given_InvalidPostId_when_DeletePost_then_ThrowNotFoundException() {
        //Given
        every { postRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            postService.deletePost(1L)
        }
        assertEquals("Post with ID 1 Not Found", ex.message)
    }
}