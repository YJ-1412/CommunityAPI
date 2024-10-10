package com.portfolio.community.repository

import com.portfolio.community.entity.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class CommentRepositoryIntegrationTest {

    @Autowired private lateinit var commentRepository: CommentRepository
    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var postRepository: PostRepository
    @Autowired private lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setUp() {
        commentRepository.deleteAll()
        boardRepository.deleteAll()
        postRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()

        val role = RoleEntity(name = "User", level = 0)
        roleRepository.save(role)

        val user = UserEntity(username = "user1", password = "password", role = role)
        userRepository.save(user)

        val board = BoardEntity(name = "Board 1", priority = 1, readableRole = role)
        boardRepository.save(board)

        val post = PostEntity(title = "Post 1", content = "Content 1", author = user, board = board)
        postRepository.save(post)

        val comment1 = CommentEntity(content = "Comment 1", author = user, post = post)
        val comment2 = CommentEntity(content = "Comment 2", author = user, post = post)
        commentRepository.save(comment1)
        commentRepository.save(comment2)
    }

    @Test
    @DisplayName("Given comments saved When findByPostId Then should return comments for the post")
    fun given_CommentsSaved_when_FindByPostId_then_ShouldReturnCommentsForThePost() {
        // When
        val post = postRepository.findAll().first()
        val result = commentRepository.findByPostIdOrderByIdAsc(post.id, PageRequest.of(0, 100))

        // Then
        assertEquals(2, result.content.size)
        assertEquals("Comment 1", result.content[0].content)
        assertEquals("Comment 2", result.content[1].content)
    }

    @Test
    @DisplayName("Given comments saved When findByUserId Then should return comments for the user")
    fun given_CommentsSaved_when_FindByUserId_then_ShouldReturnCommentsForTheUser() {
        // When
        val user = userRepository.findAll().first()
        val result = commentRepository.findByAuthorIdOrderByIdAsc(user.id, PageRequest.of(0, 100))

        // Then
        assertEquals(2, result.content.size)
        assertEquals("Comment 1", result.content[0].content)
        assertEquals("Comment 2", result.content[1].content)
    }

}