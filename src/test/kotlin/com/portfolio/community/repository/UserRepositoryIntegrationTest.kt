package com.portfolio.community.repository

import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.entity.UserEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class UserRepositoryIntegrationTest {

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()

        val role = RoleEntity(name = "User", level = 0)
        roleRepository.save(role)

        val user1 = UserEntity(username = "user1", password = "password1", role = role)
        val user2 = UserEntity(username = "user2", password = "password2", role = role)
        userRepository.save(user1)
        userRepository.save(user2)
    }

    @Test
    @DisplayName("Given users saved When findByUsername Then should return correct user")
    fun given_UsersSaved_when_FindByUsername_then_ShouldReturnCorrectUser() {
        // When
        val result = userRepository.findByUsername("user1")

        // Then
        assertNotNull(result)
        assertEquals("user1", result?.username)
        assertEquals("password1", result?.password)
    }

    @Test
    @DisplayName("Given users saved When existsByUsername Then should return true if user with username exists")
    fun given_UsersSaved_when_ExistsByUsername_then_ShouldReturnTrueIfUserWithUsernameExists() {
        // When
        val result = userRepository.existsByUsername("user1")

        // Then
        assertTrue(result)
    }

    @Test
    @DisplayName("Given no users saved When findByUsername Then should return null")
    fun given_NoUsersSaved_when_FindByUsername_then_ShouldReturnNull() {
        // Given
        userRepository.deleteAll()

        // When
        val result = userRepository.findByUsername("user1")

        // Then
        assertNull(result)
    }

    @Test
    @DisplayName("Given no users saved When existsByUsername Then should return false")
    fun given_NoUsersSaved_when_ExistsByUsername_then_ShouldReturnFalse() {
        // Given
        userRepository.deleteAll()

        // When
        val result = userRepository.existsByUsername("user1")

        // Then
        assertFalse(result)
    }
}