package com.portfolio.community.controller

import com.portfolio.community.dto.post.PostCreateRequest
import com.portfolio.community.dto.post.PostInfoResponse
import com.portfolio.community.dto.post.PostResponse
import com.portfolio.community.dto.post.PostUpdateRequest
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.PostEntity
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.service.PostService
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

class PostControllerTest {

    private lateinit var postController: PostController
    private lateinit var postService: PostService

    private val defaultRole = Role(name = "User", level = 0, id = 1)
    private val board = BoardEntity(name = "Board", priority = 0 , readableRole = defaultRole, id = 1)
    private val author = UserEntity(username = "Author", password = "password", role = defaultRole, id = 1)

    @BeforeEach
    fun setup() {
        postService = mockk()
        postController = PostController(postService)
    }

    @Test
    fun given_PostExists_then_GetAllPostInfos_then_ReturnOkAndPostInfoList() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val postInfos = (1..10).map { PostInfoResponse(PostEntity(title = "Post $it", content = "Content $it", author = author, board = board, id = it.toLong())) }
        val postInfoPage = PageImpl(postInfos, pageable, postInfos.size.toLong())
        every { postService.getAllPostInfos(0, 10) } returns postInfoPage

        //When
        val result = postController.getAllPostInfos(0, 10)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(10, result.body!!.size)
        assertEquals("Post 1", result.body!!.content[0].title)
        assertEquals(1, result.body!!.content[0].id)
    }

    @Test
    fun given_NoPostExists_then_GetAllPostInfos_then_ReturnNoContent() {
        //Given
        every { postService.getAllPostInfos(0, 10) } returns PageImpl(listOf(), PageRequest.of(0, 10), 0)

        //When
        val result = postController.getAllPostInfos(0, 10)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

    @Test
    fun given_PostExists_when_GetPostInfosByBoard_then_ReturnOkAndPostInfoList() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val postInfos = (1..10).map { PostInfoResponse(PostEntity(title = "Post $it", content = "Content $it", author = author, board = board, id = it.toLong())) }
        val postInfoPage = PageImpl(postInfos, pageable, postInfos.size.toLong())
        every { postService.getPostInfosByBoardId(1, 0, 10) } returns postInfoPage

        //When
        val result = postController.getPostInfosByBoard(1, 0, 10)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(10, result.body!!.size)
        assertEquals("Post 1", result.body!!.content[0].title)
        assertEquals(1, result.body!!.content[0].id)
    }

    @Test
    fun given_NoPostExists_when_GetPostInfosByBoard_then_ReturnNoContent() {
        //Given
        every { postService.getPostInfosByBoardId(1, 0, 10) } returns PageImpl(listOf(), PageRequest.of(0, 10), 0)

        //When
        val result = postController.getPostInfosByBoard(1, 0, 10)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

    @Test
    fun given_PostExists_when_GetPostInfosByAuthor_then_ReturnOkAndPostInfoList() {
        //Given
        val pageable = PageRequest.of(0, 10)
        val postInfos = (1..10).map { PostInfoResponse(PostEntity(title = "Post $it", content = "Content $it", author = author, board = board, id = it.toLong())) }
        val postInfoPage = PageImpl(postInfos, pageable, postInfos.size.toLong())
        every { postService.getPostInfosByAuthorId(1, 0, 10) } returns postInfoPage

        //When
        val result = postController.getPostInfosByAuthor(1, 0, 10)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(10, result.body!!.size)
        assertEquals("Post 1", result.body!!.content[0].title)
        assertEquals(1, result.body!!.content[0].id)
    }

    @Test
    fun given_NoPostExists_when_GetPostInfosByAuthor_then_ReturnNoContent() {
        //Given
        every { postService.getPostInfosByAuthorId(1, 0, 10) } returns PageImpl(listOf(), PageRequest.of(0, 10), 0)

        //When
        val result = postController.getPostInfosByAuthor(1, 0, 10)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

    @Test
    fun given_ValidData_when_GetPost_then_ReturnOkAndPost() {
        //Given
        val post = PostResponse(PostEntity(title = "Test Post", content = "Test Content", author = author, board = board, id = 1).apply { increaseViewCount() })
        every { postService.increaseViewCount(1) } returns post

        //When
        val result = postController.getPost(1)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Test Post", result.body!!.title)
        assertEquals("Test Content", result.body!!.content)
        assertEquals(1, result.body!!.viewCount)
    }

    @Test
    fun given_ValidData_when_CreatePost_then_ReturnCreatedAndNewPost() {
        //Given
        val postCreateRequest = PostCreateRequest(title = "New Post", content = "New Content", authorId = 1)
        val post = PostResponse(PostEntity(title = "New Post", content = "New Content", author = author, board = board, id = 1))
        every { postService.createPost(1, postCreateRequest) } returns post

        //When
        val result = postController.createPost(1, postCreateRequest)

        //Then
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals("/posts/1", result.headers.location!!.path)
        assertEquals("New Post", result.body!!.title)
        assertEquals("New Content", result.body!!.content)
    }

    @Test
    fun given_ValidData_when_UpdatePost_then_ReturnOkAndUpdatedPost() {
        //Given
        val postUpdateRequest = PostUpdateRequest(title = "Updated Post", content = "Updated Content", boardId = 1)
        val post = PostResponse(PostEntity(title = "Updated Post", content = "Updated Content", author = author, board = board, id = 1))
        every { postService.updatePost(1, postUpdateRequest) } returns post

        //When
        val result = postController.updatePost(1, postUpdateRequest)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Updated Post", result.body!!.title)
        assertEquals("Updated Content", result.body!!.content)
    }

    @Test
    fun given_ValidData_when_DeletePost_then_ReturnNoContent() {
        //Given
        every { postService.deletePost(1) } just Runs

        //When
        val result = postController.deletePost(1)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }
}