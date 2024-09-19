package com.portfolio.community.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.portfolio.community.JwtTestUtils
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.dto.user.UserUpdateRequest
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.repository.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
class UserControllerIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Value("\${jwt.secret}") private lateinit var secretKey: String
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var postRepository: PostRepository
    @Autowired private lateinit var commentRepository: CommentRepository

    private lateinit var level0Role: Role
    private lateinit var level1Role: Role
    private lateinit var level0User: UserEntity
    private lateinit var level1User: UserEntity
    private lateinit var staff: UserEntity
    private lateinit var admin: UserEntity

    private lateinit var jwtLV0: String
    private lateinit var jwtLV1: String
    private lateinit var jwtStaff: String
    private lateinit var jwtAdmin: String

    @BeforeEach
    fun setup() {
        boardRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()
        postRepository.deleteAll()
        commentRepository.deleteAll()

        level0Role = roleRepository.save(Role(name = "LV0", level = 0))
        level1Role = roleRepository.save(Role(name = "LV1", level = 1))
        level0User = userRepository.save(UserEntity(username = "LV0 User", password = "password", role = level0Role))
        level1User = userRepository.save(UserEntity(username = "LV1 User", password = "password", role = level1Role))
        staff = userRepository.save(UserEntity(username = "Staff", password = "password", role = level0Role).apply { setStaff() })
        admin = userRepository.save(UserEntity(username = "Admin", password = "password", role = level0Role).apply { setAdmin() })

        jwtLV0 = JwtTestUtils.generateToken(Principal(level0User), 60 * 60 * 1000, secretKey)
        jwtLV1 = JwtTestUtils.generateToken(Principal(level1User), 60 * 60 * 1000, secretKey)
        jwtStaff = JwtTestUtils.generateToken(Principal(staff), 60 * 60 * 1000, secretKey)
        jwtAdmin = JwtTestUtils.generateToken(Principal(admin), 60 * 60 * 1000, secretKey)
    }

    @Test
    fun given_ValidRequest_when_GetUser_then_ReturnOkAndUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role))

        //When
        val result = mockMvc.perform(get("/users/{userId}", testUser.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("Test User"))
            .andExpect(jsonPath("$.role.name").value("LV0"))
            .andExpect(jsonPath("$.role.level").value(0))
            .andExpect(jsonPath("$.isStaff").value(false))
            .andExpect(jsonPath("$.isAdmin").value(false))
            .andExpect(jsonPath("$.writtenPostCount").value(0))
            .andExpect(jsonPath("$.writtenCommentCount").value(0))
            .andExpect(jsonPath("$.likedPostCount").value(0))
    }

    @Test
    fun given_InvalidUserId_when_GetUser_then_ReturnNotFound() {
        //When
        val result = mockMvc.perform(get("/users/{userId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("User with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_UpdateUser_then_ReturnOkAndUpdatedUser() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "LV0 Updated User", password = "updatedPassword")

        //When
        val result = mockMvc.perform(put("/users/{userId}", level0User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("LV0 Updated User"))
            .andExpect(jsonPath("$.role.name").value("LV0"))
            .andExpect(jsonPath("$.role.level").value(0))
            .andExpect(jsonPath("$.isStaff").value(false))
            .andExpect(jsonPath("$.isAdmin").value(false))
            .andExpect(jsonPath("$.writtenPostCount").value(0))
            .andExpect(jsonPath("$.writtenCommentCount").value(0))
            .andExpect(jsonPath("$.likedPostCount").value(0))
    }

    @Test
    fun given_BlankUsername_when_UpdateUser_then_ReturnBadRequest() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "", password = "password")

        //When
        val result = mockMvc.perform(put("/users/{userId}", level0User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Username must not be blank"))
            .andExpect(jsonPath("$.details").exists())
    }


    @Test
    fun given_DuplicateUsername_when_UpdateUser_then_ReturnBadRequest() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "LV1 User", password = "password")

        //When
        val result = mockMvc.perform(put("/users/{userId}", level0User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("User with username already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserAndTargetMismatch_when_UpdateUser_then_ReturnForbidden() {
        //Given
        val userUpdateRequest = UserUpdateRequest(username = "LV0 Updated User", password = "updatedPassword")

        //When
        val result = mockMvc.perform(put("/users/{userId}", level0User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_ChangeRole_then_ReturnOkAndUpdatedUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role))

        //When
        val result = mockMvc.perform(patch("/users/{userId}/role", testUser.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(level1Role.id)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("Test User"))
            .andExpect(jsonPath("$.role.name").value("LV1"))
    }

    @Test
    fun given_UserIsNotStaffAndAdmin_when_ChangeRole_then_ReturnForbidden() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role))

        //When
        val result = mockMvc.perform(patch("/users/{userId}/role", testUser.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(level1Role.id)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidUserId_when_ChangeRole_then_ReturnNotFound() {
        //When
        val result = mockMvc.perform(patch("/users/{userId}/role", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(level1Role.id)))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("User with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidRoleId_when_ChangeRole_then_ReturnNotFound() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role))

        //When
        val result = mockMvc.perform(patch("/users/{userId}/role", testUser.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(-1)))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Role with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_TargetRoleEqualsOriginRole_when_ChangeRole_then_ReturnConflict() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role))

        //When
        val result = mockMvc.perform(patch("/users/{userId}/role", testUser.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(level0Role.id)))

        //Then
        result.andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("User already has role"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_SetStaff_then_ReturnOkAndUpdatedUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role))

        //When
        val result = mockMvc.perform(post("/staff")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUser.id)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("Test User"))
            .andExpect(jsonPath("$.role.name").value("LV0"))
            .andExpect(jsonPath("$.role.level").value(0))
            .andExpect(jsonPath("$.isStaff").value(true))
            .andExpect(jsonPath("$.isAdmin").value(false))
    }

    @Test
    fun given_InvalidUserId_when_SetStaff_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(post("/staff")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(-1)))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("User with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_TargetIsAdmin_when_SetStaff_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(post("/staff")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(admin.id)))

        //Then
        result.andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("User is already admin or staff"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAdmin_when_SetStaff_then_ReturnForbidden() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role))

        //When
        val result = mockMvc.perform(post("/staff")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUser.id)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }


    @Test
    fun given_ValidRequest_when_SetRegular_then_ReturnOkAndUpdatedUser() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role).apply { setStaff() })

        //When
        val result = mockMvc.perform(delete("/staff/{userId}", testUser.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("Test User"))
            .andExpect(jsonPath("$.role.name").value("LV0"))
            .andExpect(jsonPath("$.role.level").value(0))
            .andExpect(jsonPath("$.isStaff").value(false))
            .andExpect(jsonPath("$.isAdmin").value(false))
    }

    @Test
    fun given_InvalidUserId_when_SetRegular_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(delete("/staff/{userId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("User with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_TargetIsAdmin_when_SetRegular_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(delete("/staff/{userId}", admin.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("User is admin or already regular"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAdmin_when_SetRegular_then_ReturnForbidden() {
        //Given
        val testUser = userRepository.save(UserEntity(username = "Test User", password = "password", role = level0Role).apply { setStaff() })

        //When
        val result = mockMvc.perform(delete("/staff/{userId}", testUser.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidUserId_when_DeleteUser_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(delete("/users/{userId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_TargetIsAdmin_when_DeleteUser_then_ReturnForbidden() {
        //When
        val result = mockMvc.perform(delete("/users/{userId}", admin.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DeleteMyself_when_DeleteUser_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(delete("/users/{userId}", level0User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtLV0"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_TargetIsStaffAndUserIsAdmin_when_DeleteUser_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(delete("/users/{userId}", staff.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isNoContent)
    }

    @Test
    fun given_TargetIsRegularAndUserIsAdminOrStaff_when_DeleteUser_then_ReturnNoContent() {
        //When
        val result = mockMvc.perform(delete("/users/{userId}", level0User.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff"))

        //Then
        result.andExpect(status().isNoContent)
    }

}