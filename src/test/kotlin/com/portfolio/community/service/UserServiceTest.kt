package com.portfolio.community.service

import com.portfolio.community.dto.user.UserCreateRequest
import com.portfolio.community.dto.user.UserUpdateRequest
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var roleRepository: RoleRepository

    private val defaultRole = Role(name = "User", level = 0)

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = BCryptPasswordEncoder()
        roleRepository = mockk()
        userService = UserService(userRepository, passwordEncoder, roleRepository)

        every { roleRepository.findFirstByOrderByLevel() } returns defaultRole
    }

    @Test
    fun given_ValidData_when_CreateUser_then_CreateAndReturnNewUser() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = "password")
        val newUser = UserEntity(username = "New User", password = passwordEncoder.encode("password"), role = defaultRole, id = 1L)
        every { userRepository.existsByUsername("New User") } returns false
        every { userRepository.save(any()) } returns newUser

        //When
        val result = userService.createUser(userCreateRequest)

        //Then
        assertEquals("New User", result.username)
        assertEquals(1, result.id)
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun given_DuplicateUsername_when_CreateUser_then_ThrowIllegalArgumentException() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = "password")
        every { userRepository.existsByUsername("New User") } returns true

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            userService.createUser(userCreateRequest)
        }
        assertEquals("User with username already exists", ex.message)
    }

    @Test
    fun given_ValidData_when_GetUserById_then_ReturnUser() {
        //Given
        val user = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L)
        every { userRepository.findByIdOrNull(1L) } returns user

        //When
        val result = userService.getUserById(1L)

        //Then
        assertEquals("Test User", result.username)
        assertEquals(1, result.id)
    }

    @Test
    fun given_InvalidUserId_when_GetUserById_then_ThrowNotFoundException() {
        //Given
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.getUserById(1L)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdateUser_then_UpdateAndReturnUpdatedUser() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "Updated User", password = "password2")
        val testUser = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L)
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { userRepository.findByUsername("Updated User") } returns null

        //When
        val result = userService.updateUser(1L, userUpdateRequest)

        //Then
        assertEquals("Updated User", result.username)
        assertEquals(1, result.id)
    }

    @Test
    fun given_InvalidUserId_when_UpdateUser_then_ThrowNotFoundException() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "Updated User", password = "password2")
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.updateUser(1L, userUpdateRequest)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_DuplicateUsername_when_UpdateUser_then_ThrowIllegalArgumentException() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "Updated User", password = "password2")
        val testUser = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L)
        val duplicateUser = UserEntity(username = "Updated User", password = "password", role = defaultRole, id = 2L)
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { userRepository.findByUsername("Updated User") } returns duplicateUser

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            userService.updateUser(1L, userUpdateRequest)
        }
        assertEquals("User with username already exists", ex.message)
    }

    @Test
    fun given_ValidData_when_ChangeRole_then_ChangeRoleAndReturnUpdatedRole() {
        //Given
        val testUser = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L)
        val targetRole = Role(name = "User LV2", level = 1, id = 2L)
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { roleRepository.findByIdOrNull(2L) } returns targetRole

        //When
        val result = userService.changeRole(1, 2)

        //Then
        assertEquals("Test User", result.username)
        assertEquals(1, result.id)
        assertEquals("User LV2", result.role.name)
        assertEquals(1, result.role.level)
        assertEquals(2, result.role.id)
    }

    @Test
    fun given_InvalidUserId_when_ChangeRole_then_ThrowNotFoundException() {
        //Given
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.changeRole(1L, 2)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidRoleId_when_ChangeRole_then_ThrowNotFoundException() {
        //Given
        val testUser = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L)
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { roleRepository.findByIdOrNull(2L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.changeRole(1L, 2)
        }
        assertEquals("Role with ID 2 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_SetStaff_then_SetStaffAndReturnUpdatedUser() {
        //Given
        val testUser = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L)
        every { userRepository.findByIdOrNull(1L) } returns testUser

        //When
        val result = userService.setStaff(1)

        //Then
        assertEquals("Test User", result.username)
        assertEquals(true, result.isStaff)
        assertEquals(false, result.isAdmin)
    }

    @Test
    fun given_InvalidUserId_when_SetStaff_then_ThrowNotFoundException() {
        //Given
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.setStaff(1)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_SetRegular_then_SetRegularAndReturnUpdatedUser() {
        //Given
        val testUser = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L).apply { setStaff() }
        every { userRepository.findByIdOrNull(1L) } returns testUser

        //When
        val result = userService.setRegular(1)
        assertEquals("Test User", result.username)
        assertEquals(false, result.isStaff)
        assertEquals(false, result.isAdmin)
    }

    @Test
    fun given_InvalidUserId_when_SetRegular_then_ThrowNotFoundException() {
        //Given
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.setRegular(1)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteUser_then_DeleteUser() {
        //Given
        val testUser = UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1L)
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { userRepository.delete(any()) } returns Unit

        //When
        userService.deleteUser(1L)

        //Then
        verify(exactly = 1) { userRepository.delete(any()) }
    }

    @Test
    fun given_InvalidUserId_when_DeleteUser_then_ThrowNotFoundException() {
        //Given
        every { userRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.deleteUser(1L)
        }
        assertEquals("User with ID 1 Not Found", ex.message)
    }

}