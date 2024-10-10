package com.portfolio.community.repository

import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.PostEntity
import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.entity.UserEntity
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
class PostRepositoryIntegrationTest {

    @Autowired private lateinit var postRepository: PostRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var boardRepository: BoardRepository

    private lateinit var role: RoleEntity
    private lateinit var user: UserEntity
    private lateinit var board: BoardEntity

    @BeforeEach
    fun setUp() {
        postRepository.deleteAll()
        roleRepository.deleteAll()
        userRepository.deleteAll()
        boardRepository.deleteAll()

        role = RoleEntity(name = "User", level = 0)
        roleRepository.save(role)

        user = UserEntity(username = "user1", password = "password", role = role)
        userRepository.save(user)

        board = BoardEntity(name = "Board 1", priority = 1, readableRole = role)
        boardRepository.save(board)

        for (i in 1..10) {
            val post = PostEntity(title = "Post $i", content = "Content $i", author = user, board = board)
            postRepository.save(post)
        }
    }

    @Test
    @DisplayName("Given posts saved When findAllByOrderByCreatedDateDesc Then should return posts in descending order of creation date")
    fun given_PostsSaved_when_FindAllByOrderByCreatedDateDesc_then_ShouldReturnPostsInDescendingOrderOfCreationDate() {
        // Given
        val pageable = PageRequest.of(0, 5)

        // When
        val result = postRepository.findAllByOrderByIdDesc(pageable)

        // Then
        assertEquals(5, result.content.size)
        assertEquals("Post 10", result.content[0].title)
        assertTrue(result.isFirst)
        assertFalse(result.isLast)
    }

    @Test
    @DisplayName("Given posts saved When findByBoardIdOrderByCreatedDateDesc Then should return posts for the board in descending order of creation date")
    fun given_PostsSaved_when_FindByBoardIdOrderByCreatedDateDesc_then_ShouldReturnPostsForTheBoardInDescendingOrderOfCreationDate() {
        // Given
        val pageable = PageRequest.of(0, 5)

        // When
        val result = postRepository.findByBoardIdOrderByIdDesc(board.id, pageable)

        // Then
        assertEquals(5, result.content.size)
        assertEquals("Post 10", result.content[0].title)
        assertTrue(result.isFirst)
        assertFalse(result.isLast)
    }

    @Test
    @DisplayName("Given posts saved When findByAuthorIdOrderByCreatedDateDesc Then should return posts for the author in descending order of creation date")
    fun given_PostsSaved_when_FindByAuthorIdOrderByCreatedDateDesc_then_ShouldReturnPostsForTheAuthorInDescendingOrderOfCreationDate() {
        // Given
        val pageable = PageRequest.of(0, 5)

        // When
        val result = postRepository.findByAuthorIdOrderByIdDesc(user.id, pageable)

        // Then
        assertEquals(5, result.content.size)
        assertEquals("Post 10", result.content[0].title)
        assertTrue(result.isFirst)
        assertFalse(result.isLast)
    }

    @Test
    @DisplayName("Given posts saved When findAllByOrderByCreatedDateDesc with multiple pages Then should handle pagination correctly")
    fun given_PostsSaved_when_FindAllByOrderByCreatedDateDesc_withMultiplePages_then_ShouldHandlePaginationCorrectly() {
        // Given
        val pageableFirstPage = PageRequest.of(0, 5)
        val pageableSecondPage = PageRequest.of(1, 5)

        // When
        val resultFirstPage = postRepository.findAllByOrderByIdDesc(pageableFirstPage)
        val resultSecondPage = postRepository.findAllByOrderByIdDesc(pageableSecondPage)

        // Then
        assertEquals(5, resultFirstPage.content.size)
        assertEquals("Post 10", resultFirstPage.content[0].title)
        assertTrue(resultFirstPage.isFirst)
        assertFalse(resultFirstPage.isLast)

        assertEquals(5, resultSecondPage.content.size)
        assertEquals("Post 5", resultSecondPage.content[0].title)
        assertFalse(resultSecondPage.isFirst)
        assertTrue(resultSecondPage.isLast)
    }
}