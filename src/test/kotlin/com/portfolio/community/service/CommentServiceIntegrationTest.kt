package com.portfolio.community.service

import com.portfolio.community.dto.comment.CommentCreateRequest
import com.portfolio.community.dto.comment.CommentUpdateRequest
import com.portfolio.community.entity.*
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class CommentServiceIntegrationTest {

    @Autowired private lateinit var commentService: CommentService
    @Autowired private lateinit var commentRepository: CommentRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var postRepository: PostRepository

    private lateinit var defaultRole: RoleEntity
    private lateinit var board: BoardEntity
    private lateinit var authorOfPost: UserEntity
    private lateinit var authorOfComment: UserEntity
    private lateinit var post: PostEntity

    @BeforeEach
    fun setup() {
        commentRepository.deleteAll()
        postRepository.deleteAll()
        userRepository.deleteAll()
        boardRepository.deleteAll()
        roleRepository.deleteAll()

        defaultRole = roleRepository.save(RoleEntity(name = "User", level = 0))
        board = boardRepository.save(BoardEntity(name = "Board", priority = 0, readableRole = defaultRole))
        authorOfPost = userRepository.save(UserEntity(username = "Author of Post", password = "password", role = defaultRole))
        authorOfComment = userRepository.save(UserEntity(username = "Author of Comment", password = "password", role = defaultRole))
        post = postRepository.save(PostEntity(title = "Post", content = "Content", author = authorOfPost, board = board))
    }

    @Test
    fun given_CommentExists_when_GetCommentByPost_then_ReturnCommentPage() {
        //Given
        val comments = (1..10).map { CommentEntity(content = "Comment $it", author = authorOfComment, post = post) }
        commentRepository.saveAll(comments)

        //When
        val result = commentService.getCommentsByPost(post.id, 0, 10)

        //Then
        assertEquals(10, result.size)
        assertEquals("Comment 1", result.content[0].content)
    }

    @Test
    fun given_InvalidPostId_when_GetCommentByPost_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.getCommentsByPost(-1, 0, 10)
        }
        assertEquals("Post with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_CommentExists_when_GetCommentsByAuthor_then_ReturnPostPage() {
        //Given
        val comments = (1..10).map { CommentEntity(content = "Comment $it", author = authorOfComment, post = post) }
        commentRepository.saveAll(comments)

        //When
        val result = commentService.getCommentsByAuthor(authorOfComment.id, 0, 10)

        //Then
        assertEquals(10, result.size)
        assertEquals("Comment 1", result.content[0].content)
    }

    @Test
    fun given_InvalidPostId_when_GetCommentByAuthor_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.getCommentsByAuthor(-1, 0, 10)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_CreateComment_then_CreateAndReturnNewComment() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment")

        //When
        val result = commentService.createComment(post.id, authorOfComment.id, commentCreateRequest)

        //Then
        assertEquals("New Comment", result.content)
    }

    @Test
    fun given_InvalidAuthorId_when_CreateComment_then_ThrowNotFoundException() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment")

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.createComment(post.id, -1, commentCreateRequest)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidPostId_when_CreateComment_then_ThrowNotFoundException() {
        //Given
        val commentCreateRequest = CommentCreateRequest(content = "New Comment")

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.createComment(-1, authorOfComment.id, commentCreateRequest)
        }
        assertEquals("Post with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdateComment_then_UpdateAndReturnUpdatedComment() {
        //Given
        val testComment = commentRepository.save(CommentEntity(content = "Test Comment", author = authorOfComment, post = post))
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")

        //When
        val result = commentService.updateComment(testComment.id, commentUpdateRequest)

        //Then
        assertEquals("Updated Comment", result.content)
    }

    @Test
    fun given_InvalidCommentId_when_UpdateComment_then_ThrowNotFoundException() {
        //Given
        val commentUpdateRequest = CommentUpdateRequest(content = "Updated Comment")

        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.updateComment(-1, commentUpdateRequest)
        }
        assertEquals("Comment with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteComment_then_DeleteComment() {
        //Given
        val testComment = commentRepository.save(CommentEntity(content = "Test Comment", author = authorOfComment, post = post))

        //When
        commentService.deleteComment(testComment.id)

        //Then
        val result = commentRepository.findByIdOrNull(testComment.id)
        assertEquals(null, result)
    }

    @Test
    fun given_InvalidCommentId_when_DeleteComment_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            commentService.deleteComment(-1)
        }
        assertEquals("Comment with ID -1 Not Found", ex.message)
    }
}