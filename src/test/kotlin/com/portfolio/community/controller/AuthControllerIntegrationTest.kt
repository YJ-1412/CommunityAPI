package com.portfolio.community.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.portfolio.community.configuration.JwtTokenProvider
import com.portfolio.community.dto.RefreshTokenRequest
import com.portfolio.community.dto.user.LoginRequest
import com.portfolio.community.dto.user.UserCreateRequest
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class AuthControllerIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var jwtTokenProvider: JwtTokenProvider
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository

    private lateinit var level0Role: Role

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
        roleRepository.deleteAll()

        level0Role = roleRepository.save(Role(name = "LV0", level = 0))
    }

    @Test
    fun given_ValidRequest_when_Register_then_ReturnCreatedAndNewUser() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = "password")


        //When
        val result = mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userCreateRequest)))

        //Then
        val newUser = userRepository.findByUsername("New User")!!
        result.andExpect(header().string("Location", "/users/${newUser.id}"))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.username").value("New User"))
            .andExpect(jsonPath("$.role.name").value("LV0"))
            .andExpect(jsonPath("$.role.level").value(0))
            .andExpect(jsonPath("$.isStaff").value(false))
            .andExpect(jsonPath("$.isAdmin").value(false))
            .andExpect(jsonPath("$.writtenPostCount").value(0))
            .andExpect(jsonPath("$.writtenCommentCount").value(0))
            .andExpect(jsonPath("$.likedPostCount").value(0))
    }

    @Test
    fun given_BlankUsername_when_Signup_then_ReturnBadRequest() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "", password = "password")

        //When
        val result = mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Username must not be blank"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicateUsername_when_Signup_then_ReturnBadRequest() {
        //Given
        userRepository.save(UserEntity(username = "New User", password = "password", role = level0Role))
        val userCreateRequest = UserCreateRequest(username = "New User", password = "password")

        //When
        val result = mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("User with username already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_BlankPassword_when_Signup_then_ReturnBadRequest() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = null)

        //When
        val result = mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Password must not be blank"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_PasswordIsShorterThan8_when_Signup_then_ReturnBadRequest() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = "1234567")

        //When
        val result = mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Password must be between 8 and 20 characters"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_PasswordIsLongerThan20_when_Signup_then_ReturnBadRequest() {
        //Given
        val userCreateRequest = UserCreateRequest(username = "New User", password = "123456789012345678901")

        //When
        val result = mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Password must be between 8 and 20 characters"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_Login_then_ReturnOkAndJwtToken() {
        //Given
        userRepository.save(UserEntity(username = "Test User", password = BCryptPasswordEncoder().encode("password"), role = level0Role))
        val loginRequest = LoginRequest(username = "Test User", password = "password")

        //When
        val result = mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk)
            .andReturn()

        //Then
        val responseJson = result.response.contentAsString
        val tokenMap = ObjectMapper().readValue(responseJson, Map::class.java)
        val accessToken = tokenMap["accessToken"].toString()
        val refreshToken = tokenMap["refreshToken"].toString()

        assertTrue(jwtTokenProvider.validateToken(accessToken))
        assertTrue(jwtTokenProvider.validateToken(refreshToken))

        val username = jwtTokenProvider.getUsername(accessToken)

        assertEquals("Test User", username)
    }

    @Test
    fun given_InvalidUsername_when_Login_then_ReturnOkAndJwtToken() {
        //Given
        val loginRequest = LoginRequest(username = "Test User", password = "password")

        //When
        val result = mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))

        //Then
        result.andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Bad credentials"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidPassword_when_Login_then_ReturnOkAndJwtToken() {
        //Given
        userRepository.save(UserEntity(username = "Test User", password = BCryptPasswordEncoder().encode("password"), role = level0Role))
        val loginRequest = LoginRequest(username = "Test User", password = "wrongpassword")

        //When
        val result = mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))

        //Then
        result.andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("Bad credentials"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_RefreshToken_then_ReturnOkAndNewAccessToken() {
        //Given
        userRepository.save(UserEntity(username = "Test User", password = BCryptPasswordEncoder().encode("password"), role = level0Role))
        val loginRequest = LoginRequest(username = "Test User", password = "password")

        val loginResult = mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
            .andReturn()

        val loginResponseJson = loginResult.response.contentAsString
        val loginTokenMap = ObjectMapper().readValue(loginResponseJson, Map::class.java)
        val refreshToken = loginTokenMap["refreshToken"].toString()
        val refreshTokenRequest = RefreshTokenRequest(refreshToken)

        //When
        val result = mockMvc.perform(post("/refresh-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(refreshTokenRequest)))
            .andExpect(status().isOk)
            .andReturn()

        //Then
        val responseJson = result.response.contentAsString
        val tokenMap = ObjectMapper().readValue(responseJson, Map::class.java)
        val newAccessToken = tokenMap["accessToken"].toString()

        assertTrue(jwtTokenProvider.validateToken(newAccessToken))
    }

}