package com.portfolio.community.repository

import com.portfolio.community.entity.RoleEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class RoleEntityRepositoryIntegrationTest {

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setUp() {
        roleRepository.deleteAll()

        val level1Role = RoleEntity(name = "Lv1", level = 0)
        val level2Role = RoleEntity(name = "Lv2", level = 1)
        roleRepository.save(level1Role)
        roleRepository.save(level2Role)
    }

    @Test
    fun given_RolesSaved_when_FindByLevel_then_ReturnCorrectRole() {
        // When
        val level1Role = roleRepository.findByLevel(0)

        // Then
        assertNotNull(level1Role)
        assertEquals("Lv1", level1Role?.name)
    }

    @Test
    fun given_RolesSaved_when_FindFirstByLevel_then_ReturnCorrectRole() {
        //When
        val lowestRole = roleRepository.findFirstByOrderByLevel()

        //Then
        assertNotNull(lowestRole)
        assertEquals("Lv1", lowestRole?.name)
        assertEquals(0, lowestRole?.level)
    }

    @Test
    fun given_RolesSaved_when_FindByName_then_ReturnCorrectRole() {
        // When
        val level2Role = roleRepository.findByName("Lv2")

        // Then
        assertNotNull(level2Role)
        assertEquals(1, level2Role?.level)
    }

    @Test
    fun given_RolesSaved_when_FindAllByOrderByLevel_then_ReturnRolesInOrderOfLevel() {
        // When
        val result = roleRepository.findAllByOrderByLevel()

        // Then
        assertEquals(2, result.size)
        assertEquals("Lv1", result[0].name)
        assertEquals(0, result[0].level)
        assertEquals("Lv2", result[1].name)
        assertEquals(1, result[1].level)
    }

    @Test
    fun given_RolesSaved_when_ExistsByLevel_then_ReturnTrueIfRoleWithLevelExists() {
        // When
        val result = roleRepository.existsByLevel(1)

        // Then
        assertTrue(result)
    }

    @Test
    fun given_RolesSaved_when_ExistsByName_then_ReturnTrueIfRoleWithNameExists() {
        // When
        val result = roleRepository.existsByName("Lv1")

        // Then
        assertTrue(result)
    }
}