package com.portfolio.community.service

import com.portfolio.community.dto.comment.*
import com.portfolio.community.entity.*
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.CommentRepository
import com.portfolio.community.repository.PostRepository
import com.portfolio.community.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

class CommentServiceTest {

    private lateinit var commentService: CommentService
    private lateinit var commentRepository: CommentRepository
    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository

    private lateinit var post: PostEntity
    private lateinit var author: UserEntity

    @BeforeEach
    fun setUp() {
        commentRepository = mockk()
        postRepository = mockk()
        userRepository = mockk()
        commentService = CommentService(commentRepository, postRepository, userRepository)

        val role = Role(name = "User", level = 0)
        val board = BoardEntity(name = "Board 1", priority = 1, readableRole = role)

        author = UserEntity(username =  "User 1", password = "password", role = role)
        post = PostEntity(title = "Post 1", content = "Post 1 content", author = author, board = board)
    }

    @Test
    fun given_PostAndAuthorAndCommentExists_when_GetCommentsByPost_then_ReturnCommentPageOnPost() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val comments = (1..10).map { CommentEntity(content = "Comment $it", author = author, post = post) }
        val commentPage: Page<CommentEntity> = PageImpl(comments, pageable, comments.size.toLong())
        every { postRepository.existsById(1L) } returns true
        every { commentRepository.findByPostIdOrderByIdAsc(1L, pageable) } returns commentPage

        //When
        val result = commentService.getCommentsByPost(1L, 0, 10)

        //Then
        assertEquals(10, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals(comments.map { CommentByPostResponse(it) }, result.content)
    }

    @Test
    fun given_InvalidPostId_when_GetCommentsByPost_then_ThrowNotFoundException() {
        //Given
        every { postRepository.existsById(1L) } returns false

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.getCommentsByPost(1L, 0, 10)
        }
        assertEquals("Post with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_PostAndAuthorAndCommentExists_when_GetCommentsByAuthor_then_ReturnCommentPageByAuthor() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val comments = (1..10).map { CommentEntity(content = "Comment $it", author = author, post = post) }
        val commentPage: Page<CommentEntity> = PageImpl(comments, pageable, comments.size.toLong())
        every { userRepository.existsById(1L) } returns true
        every { commentRepository.findByAuthorIdOrderByIdAsc(1L, pageable) } returns commentPage

        //When
        val result = commentService.getCommentsByAuthor(1L, 0, 10)

        //Then
        assertEquals(10, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals(comments.map { CommentByAuthorResponse(it) }, result.content)
    }

    @Test
    fun given_InvalidAuthorId_when_GetCommentsByAuthor_then_ThrowNotFoundException() {
        //Given
        every { userRepository.existsById(1L) } returns false

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.getCommentsByAuthor(1L, 0, 10)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_CreateComment_then_CreateAndReturnNewComment() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment")
        val newComment = CommentEntity(content = "New Comment", author = author, post = post, id = 1L)
        every { userRepository.findByIdOrNull(1L) } returns author
        every { postRepository.findByIdOrNull(1L) } returns post
        every { commentRepository.save(any()) } returns newComment

        //When
        val result = commentService.createComment(1L, 1L, commentCreateRequest)

        //Then
        assertEquals("New Comment", result.content)
        assertEquals(1L, result.id)
        verify(exactly = 1) { commentRepository.save(any()) }
    }

    @Test
    fun given_InvalidAuthorId_when_CreateComment_then_ThrowNotFoundException() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment")
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.createComment(1L, 1L, commentCreateRequest)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidPostId_when_CreateComment_then_ThrowNotFoundException() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment")
        every { userRepository.findByIdOrNull(1L) } returns author
        every { postRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.createComment(1L, 1L, commentCreateRequest)
        }
        assertEquals("Post with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdateComment_then_UpdateAndReturnUpdatedComment() {
        //Given
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")
        val oldComment = CommentEntity(content = "Old Comment", author = author, post = post, id = 1L)
        every { commentRepository.findByIdOrNull(1L) } returns oldComment

        //When
        val result = commentService.updateComment(1L, commentUpdateRequest)

        //Then
        assertEquals("Updated Comment", result.content)
        assertEquals(1L, result.id)
    }

    @Test
    fun given_InvalidCommentId_when_UpdateComment_then_ThrowNotFoundException() {
        //Given
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")
        every { commentRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.updateComment(1L, commentUpdateRequest)
        }
        assertEquals("Comment with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteComment_then_DeleteComment() {
        //Given
        val testComment = CommentEntity(content = "Test Comment", author = author, post = post, id = 1L)
        every { commentRepository.findByIdOrNull(1L) } returns testComment
        every { commentRepository.delete(any()) } returns Unit

        //When
        commentService.deleteComment(1L)

        //Then
        verify { commentRepository.delete(any()) }
    }

    @Test
    fun given_InvalidCommentId_when_DeleteComment_then_ThrowNotFoundException() {
        //Given
        CommentEntity(content = "Test Comment", author = author, post = post, id = 1L)
        every { commentRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.deleteComment(1L)
        }
        assertEquals("Comment with ID 1 Not Found", ex.message)
    }
}