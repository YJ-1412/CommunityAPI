package com.portfolio.community.controller

import com.portfolio.community.configuration.JwtTokenProvider
import com.portfolio.community.dto.user.UserResponse
import com.portfolio.community.dto.user.UserUpdateRequest
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.service.UserService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager

class UserControllerTest {

    private lateinit var userController: UserController
    private lateinit var userService: UserService
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authenticationManager: AuthenticationManager

    private val defaultRole = Role(name = "User", level = 0, id = 1)

    @BeforeEach
    fun setup() {
        userService = mockk()
        jwtTokenProvider = mockk()
        authenticationManager = mockk()
        userController = UserController(userService)
    }

    @Test
    fun given_ValidData_when_GetUser_then_ReturnOkAndUser() {
        //Given
        val testUser = UserResponse(UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1))
        every { userService.getUserById(1) } returns testUser

        //When
        val result = userController.getUser(1)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Test User", result.body!!.username)
    }

    @Test
    fun given_ValidData_when_UpdateUser_then_ReturnOkAndUpdatedUser() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "Updated User", password = "password")
        val updatedUser = UserResponse(UserEntity(username = "Updated User", password = "password", role = defaultRole, id = 1))
        every { userService.updateUser(1, userUpdateRequest) } returns updatedUser

        //When
        val result = userController.updateUser(1, userUpdateRequest)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Updated User", result.body!!.username)
    }

    @Test
    fun given_ValidData_when_ChangeRole_then_ReturnOkAndUpdatedUser() {
        //Given
        val targetRole = Role(name = "User Lv2", level = 1, id = 2)
        val updatedUser = UserResponse(UserEntity(username = "Test User", password = "password", role = targetRole, id = 1))
        every { userService.changeRole(1, 2) } returns updatedUser

        //When
        val result = userController.changeRole(1, 2)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Test User", result.body!!.username)
        assertEquals("User Lv2", result.body!!.role.name)
    }

    @Test
    fun given_ValidData_when_SetStaff_then_ReturnOkAndUpdatedUser() {
        //Given
        val updatedUser = UserResponse(UserEntity(username = "Staff", password = "password", role = defaultRole, id = 1).apply { setStaff() })
        every { userService.setStaff(1) } returns updatedUser

        //When
        val result = userController.setStaff(1)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Staff", result.body!!.username)
        assertEquals(true, result.body!!.isStaff)
    }

    @Test
    fun given_ValidData_when_SetRegular_then_ReturnOkAndUpdatedUser() {
        //Given
        val updatedUser = UserResponse(UserEntity(username = "Test User", password = "password", role = defaultRole, id = 1))
        every { userService.setRegular(1) } returns updatedUser

        //When
        val result = userController.setRegular(1)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Test User", result.body!!.username)
        assertEquals(false, result.body!!.isStaff)
    }

    @Test
    fun given_ValidData_when_DeleteUser_then_ReturnNoContent() {
        //Given
        every { userService.deleteUser(1) } just Runs

        //When
        val result = userController.deleteUser(1)

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

}