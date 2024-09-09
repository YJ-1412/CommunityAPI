package com.portfolio.community.controller

import com.portfolio.community.dto.comment.*
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.entity.*
import com.portfolio.community.service.CommentService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus

class CommentControllerTest {

    private lateinit var commentController: CommentController
    private lateinit var commentService: CommentService

    private val defaultRole = Role(name = "User", level = 0, id = 1L)
    private val board = BoardEntity(name = "Board", priority = 0, readableRole = defaultRole, id = 1L)
    private val author = UserEntity(username = "Author", password = "password", role = defaultRole, id = 1L)
    private val post = PostEntity(title = "Post", content = "Content", author = author, board = board, id = 1L)

    @BeforeEach
    fun setUp() {
        commentService = mockk()
        commentController = CommentController(commentService)
    }

    @Test
    fun given_CommentExists_when_GetCommentsByPost_then_ReturnOkAndCommentList() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val comments = (1..10).map { CommentByPostResponse(CommentEntity(content = "Comment $it", author = author, post = post, id = 1L)) }
        val commentPage = PageImpl(comments, pageable, comments.size.toLong())
        every { commentService.getCommentsByPost(1, 0, 10) } returns commentPage

        //When
        val result = commentController.getCommentsByPost(1, 0, 10)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(10, result.body!!.size)
        assertEquals("Comment 1", result.body!!.content[0].content)
    }

    @Test
    fun given_NoCommentExists_when_GetCommentsByPost_then_ReturnNoContent() {
        //Given
        every { commentService.getCommentsByPost(1, 0, 10) } returns PageImpl(listOf(), PageRequest.of(0, 10), 0)

        //When
        val result = commentController.getCommentsByPost(1, 0, 10)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

    @Test
    fun given_CommentExists_when_GetCommentsByAuthor_then_ReturnOkAndCommentList() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val comments = (1..10).map { CommentByAuthorResponse(CommentEntity(content = "Comment $it", author = author, post = post, id = 1L)) }
        val commentPage = PageImpl(comments, pageable, comments.size.toLong())
        every { commentService.getCommentsByAuthor(1, 0, 10) } returns commentPage

        //When
        val result = commentController.getCommentsByAuthor(1, 0, 10)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(10, result.body!!.size)
        assertEquals("Comment 1", result.body!!.content[0].content)
    }

    @Test
    fun given_NoCommentExists_when_GetCommentsByAuthor_then_ReturnNoContent() {
        //Given
        every { commentService.getCommentsByAuthor(1, 0, 10) } returns PageImpl(listOf(), PageRequest.of(0, 10), 0)

        //When
        val result = commentController.getCommentsByAuthor(1, 0, 10)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

    @Test
    fun given_ValidData_when_CreateComment_then_ReturnCreatedAndNewComment() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment")
        val newComment = CommentResponse(CommentEntity(content = "New Comment", author = author, post = post, id = 1L))
        every { commentService.createComment(1, 1, commentCreateRequest) } returns newComment

        //When
        val result = commentController.createComment(1, commentCreateRequest, Principal(author))

        //Then
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals("/posts/1/comments/1", result.headers.location!!.path)
        assertEquals("New Comment", result.body!!.content)
    }

    @Test
    fun given_ValidData_when_UpdateComment_then_ReturnOkAndUpdatedComment() {
        //Given
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")
        val updatedComment = CommentResponse(CommentEntity(content = "Updated Comment", author = author, post = post, id = 1L))
        every { commentService.updateComment(1, commentUpdateRequest) } returns updatedComment

        //When
        val result = commentController.updateComment(1, commentUpdateRequest)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Updated Comment", result.body!!.content)
    }

    @Test
    fun given_ValidData_when_DeleteComment_then_ReturnNoContent() {
        //Given
        every { commentService.deleteComment(1) } just Runs

        //When
        val result = commentController.deleteComment(1)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }
}