package com.portfolio.community.repository

import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.Role
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BoardRepositoryIntegrationTest {

    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setUp() {
        // Given: 데이터 초기화
        boardRepository.deleteAll()
        roleRepository.deleteAll()

        val role = Role(name = "User", level = 0)
        roleRepository.save(role)

        val board1 = BoardEntity(name = "Board 1", priority = 1, role)
        val board2 = BoardEntity(name = "Board 2", priority = 2, role)
        boardRepository.save(board1)
        boardRepository.save(board2)
    }

    @Test
    @DisplayName("Given boards saved When findAllByOrderByPriorityAsc Then should return boards in priority order")
    fun given_BoardsSaved_when_FindAllByOrderByPriorityAsc_then_ShouldReturnBoardsInPriorityOrder() {
        // When: 메서드 호출
        val boards = boardRepository.findAllByOrderByPriority()

        // Then: 결과 검증
        assertEquals(2, boards.size)
        assertEquals("Board 1", boards[0].name)
        assertEquals(1, boards[0].priority)
        assertEquals("Board 2", boards[1].name)
        assertEquals(2, boards[1].priority)
    }

    @Test
    @DisplayName("Given board saved When findByName Then should return correct board")
    fun given_BoardSaved_when_FindByName_then_ShouldReturnCorrectBoard() {
        // When: 메서드 호출
        val result = boardRepository.findByName("Board 1")

        // Then: 결과 검증
        assertNotNull(result)
        assertEquals("Board 1", result?.name)
        assertEquals(1, result?.priority)
    }

    @Test
    @DisplayName("Given board saved When existsByName Then should return true")
    fun given_BoardSaved_when_ExistsByName_then_ShouldReturnTrue() {
        // When: 메서드 호출
        val result = boardRepository.existsByName("Board 1")

        // Then: 결과 검증
        assertTrue(result)
    }

    @Test
    @DisplayName("Given board saved When findByPriority Then should return correct board")
    fun given_BoardSaved_when_FindByPriority_then_ShouldReturnCorrectBoard() {
        // When: 메서드 호출
        val result = boardRepository.findByPriority(1)

        // Then: 결과 검증
        assertNotNull(result)
        assertEquals("Board 1", result?.name)
        assertEquals(1, result?.priority)
    }

    @Test
    @DisplayName("Given board saved When existsByPriority Then should return true")
    fun given_BoardSaved_when_ExistsByPriority_then_ShouldReturnTrue() {
        // When: 메서드 호출
        val result = boardRepository.existsByPriority(1)

        // Then: 결과 검증
        assertTrue(result)
    }
}