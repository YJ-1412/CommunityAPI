package com.portfolio.community.service

import com.portfolio.community.dto.user.UserCreateRequest
import com.portfolio.community.dto.user.UserUpdateRequest
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestPropertySource(locations = ["classpath:application-test.properties"])
class UserServiceIntegrationTest {

    @Autowired private lateinit var userService: UserService

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository

    private lateinit var defaultRole: Role

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        roleRepository.deleteAll()
        defaultRole = roleRepository.save(Role(name = "User LV0", level = 0))
    }

    @Test
    fun given_ValidData_when_CreateUser_then_CreateAndReturnNewUser() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = "password")

        //When
        val result = userService.createUser(userCreateRequest)

        //Then
        assertEquals("New User", result.username)
    }

    @Test
    fun given_DuplicatedUsername_when_CreateUser_then_ThrowIllegalArgumentException() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = "password")
        userRepository.save(UserEntity(username = "New User", password = "password", role = defaultRole))

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            userService.createUser(userCreateRequest)
        }
        assertEquals("User with username already exists", ex.message)
    }

    @Test
    fun given_NoRoleExists_when_CreateUser_then_ThrowIllegalArgumentException() {
        //Given
        roleRepository.deleteAll()
        val userCreateRequest = UserCreateRequest(username = "New User", password = "password")

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            userService.createUser(userCreateRequest)
        }
        assertEquals("No role exists", ex.message)
    }

    @Test
    fun given_ValidData_when_GetUserById_then_ReturnUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When
        val result = userService.getUserById(testUser.id)

        //Then
        assertEquals("Test User", result.username)
    }

    @Test
    fun given_InvalidUserId_when_GetUserById_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.getUserById(-1)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdateUser_then_UpdateAndReturnUpdatedUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        val userUpdateRequest = UserUpdateRequest(username = "Updated User", password = "updatedPassword")

        //When
        val result = userService.updateUser(testUser.id, userUpdateRequest)

        //Then
        assertEquals("Updated User", result.username)
    }

    @Test
    fun given_InvalidUserId_when_UpdateUser_then_ThrowNotFoundException() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "Updated User", password = "password")

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.updateUser(-1, userUpdateRequest)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_DuplicatedUsername_when_UpdateUser_then_ThrowIllegalArgumentException() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        userRepository.save(UserEntity(username = "Updated User", password = "password", role = defaultRole))
        val userUpdateRequest = UserUpdateRequest(username = "Updated User", password = "updatedPassword")

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            userService.updateUser(testUser.id, userUpdateRequest)
        }
        assertEquals("User with username already exists", ex.message)
    }

    @Test
    fun given_ValidData_when_ChangeRole_then_ChangeRoleAndReturnUpdatedUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))
        val targetRole = roleRepository.save(Role(name = "User LV1", level = 1))

        //When
        val result = userService.changeRole(testUser.id, targetRole.id)

        //Then
        assertEquals("Test User", result.username)
        assertEquals("User LV1", result.role.name)
    }

    @Test
    fun given_InvalidUserId_when_ChangeRole_then_ThrowNotFoundException() {
        //Given
        val targetRole = roleRepository.save(Role(name = "User LV1", level = 1))

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.changeRole(-1, targetRole.id)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_InvalidRoleId_when_ChangeRole_then_ThrowNotFoundException() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.changeRole(testUser.id, -1)
        }
        assertEquals("Role with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_UserAlreadyHasRole_when_ChangeRole_then_ThrowIllegalStateException() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When & Then
        val ex = assertThrows<IllegalStateException> {
            userService.changeRole(testUser.id, defaultRole.id)
        }
        assertEquals("User already has role", ex.message)
    }

    @Test
    fun given_ValidData_when_SetStaff_then_SetStaffAndReturnUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When
        val result = userService.setStaff(testUser.id)

        //Then
        assertEquals("Test User", result.username)
        assertEquals(true, result.isStaff)
    }

    @Test
    fun given_InvalidUserId_when_SetStaff_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.setStaff(-1)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_UserIsAlreadyAdminOrStaff_when_SetStaff_then_ThrowIllegalStateException() {
        //Given
        val adminUser = userRepository.save(UserEntity(username = "Admin", password = "password", role = defaultRole).apply { setAdmin() })
        val staffUser = userRepository.save(UserEntity(username = "Staff", password = "password", role = defaultRole).apply { setStaff() })

        //When & Then
        val adminEx = assertThrows<IllegalStateException> {
            userService.setStaff(adminUser.id)
        }
        assertEquals("User is already admin or staff", adminEx.message)
        val staffEx = assertThrows<IllegalStateException> {
            userService.setStaff(staffUser.id)
        }
        assertEquals("User is already admin or staff", staffEx.message)
    }

    @Test
    fun given_ValidData_when_SetRegular_then_SetRegularAndReturnUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole).apply { setStaff() })

        //When
        val result = userService.setRegular(testUser.id)

        //Then
        assertEquals("Test User", result.username)
        assertEquals(false, result.isStaff)
    }

    @Test
    fun given_InvalidUserId_when_SetRegular_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.setRegular(-1)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_UserIsAlreadyRegular_when_SetRegular_then_ThrowIllegalStateException() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When & Then
        val ex = assertThrows<IllegalStateException> {
            userService.setRegular(testUser.id)
        }
        assertEquals("User is already regular", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteUser_then_DeleteUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When
        userService.deleteUser(testUser.id)

        //Then
        assertEquals(0, userRepository.count())
    }

    @Test
    fun given_InvalidUserId_when_DeleteUser_then_ThrowNotFoundException() {
        //When & Then
        val ex = assertThrows<NotFoundException> {
            userService.deleteUser(-1)
        }
        assertEquals("User with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_LoadUserByUsername_then_ReturnUserDetails() {
        //Given
        userRepository.save(UserEntity(username = "Test User", password = "password", role = defaultRole))

        //When
        val result = userService.loadUserByUsername("Test User")

        //Then
        assertEquals("Test User", result.username)
        assertEquals("password", result.password)
    }
}