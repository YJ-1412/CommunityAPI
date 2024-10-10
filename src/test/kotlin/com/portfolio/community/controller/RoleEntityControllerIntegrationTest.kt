package com.portfolio.community.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.portfolio.community.JwtTestUtils
import com.portfolio.community.dto.role.RoleBatchUpdateRequest
import com.portfolio.community.dto.role.RoleCreateRequest
import com.portfolio.community.dto.role.RoleUpdateRequest
import com.portfolio.community.dto.user.Principal
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.repository.BoardRepository
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
class RoleEntityControllerIntegrationTest {

    @Autowired private lateinit var mockMvc: MockMvc
    @Value("\${jwt.secret}") private lateinit var secretKey: String
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Autowired private lateinit var boardRepository: BoardRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var roleRepository: RoleRepository

    private lateinit var level0Role: RoleEntity
    private lateinit var level0User: UserEntity
    private lateinit var staff: UserEntity
    private lateinit var admin: UserEntity

    private lateinit var jwtLV0: String
    private lateinit var jwtStaff: String
    private lateinit var jwtAdmin: String

    @BeforeEach
    fun setup() {
        boardRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()

        level0Role = roleRepository.save(RoleEntity(name = "LV0", level = 0))
        admin = userRepository.save(UserEntity(username = "Admin", password = "00000000", role = level0Role).apply { setAdmin() })
        staff = userRepository.save(UserEntity(username = "Staff", password = "00000000", role = level0Role).apply { setStaff() })
        level0User = userRepository.save(UserEntity(username = "LV0 User", password = "00000000", role = level0Role))

        jwtLV0 = JwtTestUtils.generateToken(Principal(level0User), 60 * 60 * 1000, secretKey)
        jwtStaff = JwtTestUtils.generateToken(Principal(staff), 60 * 60 * 1000, secretKey)
        jwtAdmin = JwtTestUtils.generateToken(Principal(admin), 60000, secretKey)
    }

    @Test
    fun given_RoleExists_when_GetAllRoles_then_ReturnOkAndRoleList() {
        //Given
        roleRepository.saveAll((1..5).map { RoleEntity(name = "LV$it", level = it) })

        //When
        val result = mockMvc.perform(get("/roles"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(6))
            .andExpect(jsonPath("$[0].name").value("LV0"))
            .andExpect(jsonPath("$[0].level").value(0))
            .andExpect(jsonPath("$[1].name").value("LV1"))
            .andExpect(jsonPath("$[1].level").value(1))
    }

    @Test
    fun given_ValidRequest_when_CreateRole_then_ReturnCreatedAndNewRole() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "LV1", level = 1)

        //When
        val result = mockMvc.perform(post("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleCreateRequest)))

        //Then
        result.andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("LV1"))
            .andExpect(jsonPath("$.level").value(1))
    }

    @Test
    fun given_DuplicateName_when_CreateRole_then_ReturnBadRequest() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "LV0", level = 1)

        //When
        val result = mockMvc.perform(post("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Role with name already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_BlankName_when_CreateRole_then_ReturnBadRequest() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "", level = 1)

        //When
        val result = mockMvc.perform(post("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Name must not be blank"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicateLevel_when_CreateRole_then_ReturnBadRequest() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "LV1", level = 0)

        //When
        val result = mockMvc.perform(post("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Role with level already exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_NullLevel_when_CreateRole_then_ReturnBadRequest() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "LV1", level = null)

        //When
        val result = mockMvc.perform(post("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Level must not be null"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidLevel_when_CreateRole_then_ReturnBadRequest() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "LV1", level = -1)

        //When
        val result = mockMvc.perform(post("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleCreateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Level must be greater than or equal to 0"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAdmin_when_CreateRole_then_ReturnForbidden() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "LV1", level = 1)

        //When
        val result = mockMvc.perform(post("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleCreateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_UpdateRole_then_ReturnOkAndUpdatedRole() {
        //Given
        val testRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "LV2", level = 2)

        //When
        val result = mockMvc.perform(put("/roles/{roleId}", testRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("LV2"))
            .andExpect(jsonPath("$.level").value(2))
    }

    @Test
    fun given_InvalidRoleId_when_UpdateRole_then_ReturnNotFound() {
        //Given
        val roleUpdateRequest = RoleUpdateRequest(name = "LV2", level = 2)

        //When
        val result = mockMvc.perform(put("/roles/{roleId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest)))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Role with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicateName_when_UpdateRole_then_ReturnBadRequest() {
        //Given
        val testRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "LV0", level = 2)

        //When
        val result = mockMvc.perform(put("/roles/{roleId}", testRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Role with name already exists"))
            .andExpect(jsonPath("$.details").exists())
    }


    @Test
    fun given_BlankName_when_UpdateRole_then_ReturnBadRequest() {
        //Given
        val testRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "", level = 2)

        //When
        val result = mockMvc.perform(put("/roles/{roleId}", testRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Name must not be blank"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_DuplicateLevel_when_UpdateRole_then_ReturnBadRequest() {
        //Given
        val testRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "LV2", level = 0)

        //When
        val result = mockMvc.perform(put("/roles/{roleId}", testRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Role with level already exists"))
            .andExpect(jsonPath("$.details").exists())
    }


    @Test
    fun given_NullLevel_when_UpdateRole_then_ReturnBadRequest() {
        //Given
        val testRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "LV2", level = null)

        //When
        val result = mockMvc.perform(put("/roles/{roleId}", testRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest)))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Level must not be null"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAdmin_when_UpdateRole_then_ReturnForbidden() {
        //Given
        val testRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "LV2", level = 2)

        //When
        val result = mockMvc.perform(put("/roles/{roleId}", testRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(roleUpdateRequest)))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_DeleteRole_then_ReturnOkAndMoveToDefaultRole() {
        //Given
        val testRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = testRole) })
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = testRole) })

        //When
        val result = mockMvc.perform(delete("/roles/{roleId}", testRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("LV0"))
            .andExpect(jsonPath("$.level").value(0))
        val board = boardRepository.findByName("Board 1")!!
        val user = userRepository.findByUsername("User 1")!!
        assertEquals("LV0", board.readableRole.name)
        assertEquals("LV0", user.role.name)
    }

    @Test
    fun given_InvalidRoleId_when_DeleteRole_then_ReturnNullFound() {
        //When
        val result = mockMvc.perform(delete("/roles/{roleId}", -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Role with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_OnlyOneRoleExists_when_DeleteRole_then_ReturnConflict() {
        //When
        val result = mockMvc.perform(delete("/roles/{roleId}", level0Role.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("At least one role must exists"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_DeleteRoleAndMoveBoardsAndUsers_then_ReturnOkAndReturnTargetRole() {
        //Given
        val sourceRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = sourceRole) })
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = sourceRole) })
        val targetRole = roleRepository.save(RoleEntity(name = "LV2", level = 2))

        //When
        val result = mockMvc.perform(delete("/roles/{sourceRoleId}/transfer/{targetRoleId}", sourceRole.id, targetRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("LV2"))
            .andExpect(jsonPath("$.level").value(2))
        val board = boardRepository.findByName("Board 1")!!
        val user = userRepository.findByUsername("User 1")!!
        assertEquals("LV2", board.readableRole.name)
        assertEquals("LV2", user.role.name)
    }

    @Test
    fun given_SourceAndTargetEquals_when_DeleteRoleAndMoveBoardsAndUsers_then_ReturnBadRequest() {
        //Given
        val sourceRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = sourceRole) })
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = sourceRole) })

        //When
        val result = mockMvc.perform(delete("/roles/{sourceRoleId}/transfer/{targetRoleId}", sourceRole.id, sourceRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Source role and target role must be different"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidSourceRoleId_when_DeleteRoleAndMoveBoardsAndUsers_then_ReturnNotFound() {
        val targetRole = roleRepository.save(RoleEntity(name = "LV2", level = 2))

        //When
        val result = mockMvc.perform(delete("/roles/{sourceRoleId}/transfer/{targetRoleId}", -1, targetRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Role with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_InvalidTargetRoleId_when_DeleteRoleAndMoveBoardsAndUsers_then_ReturnNotFound() {
        //Given
        val sourceRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = sourceRole) })
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = sourceRole) })

        //When
        val result = mockMvc.perform(delete("/roles/{sourceRoleId}/transfer/{targetRoleId}", sourceRole.id, -1)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin"))

        //Then
        result.andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Role with ID -1 Not Found"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_UserIsNotAdmin_when_DeleteRoleAndMoveBoardsAndUsers_then_ReturnForbidden() {
        //Given
        val sourceRole = roleRepository.save(RoleEntity(name = "LV1", level = 1))
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = sourceRole) })
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = sourceRole) })
        val targetRole = roleRepository.save(RoleEntity(name = "LV2", level = 2))

        //When
        val result = mockMvc.perform(delete("/roles/{sourceRoleId}/transfer/{targetRoleId}", sourceRole.id, targetRole.id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtStaff"))

        //Then
        result.andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("Access Denied"))
            .andExpect(jsonPath("$.details").exists())
    }

    @Test
    fun given_ValidRequest_when_BatchUpdateRole_then_ReturnOkAndRoleList() {
        //Given
        //level 0~6까지 존재. 0~4를 1씩 올린 뒤, 새로운 0을 생성, 그리고 5는 삭제하며 6에 병합
        val roles = mutableListOf(level0Role)
        roles.addAll(roleRepository.saveAll((1..6).map { RoleEntity(name = "LV$it", level = it) }))
        val batchUpdateRequest = RoleBatchUpdateRequest(
            updates = (0..4).map { Pair(roles[it].id, RoleUpdateRequest(name = "Updated LV${it+1}", level = it+1)) },
            creates = listOf(RoleCreateRequest(name = "New LV0", level = 0)),
            moves = listOf(Pair(roles[5].id, roles[6].id))
        )
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = roles[5]) })
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = roles[5]) })

        //When
        val result = mockMvc.perform(put("/roles")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $jwtAdmin")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(batchUpdateRequest)))

        //Then
        result.andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("New LV0"))
            .andExpect(jsonPath("$[0].level").value(0))
            .andExpect(jsonPath("$[1].name").value("Updated LV1"))
            .andExpect(jsonPath("$[1].level").value(1))
            .andExpect(jsonPath("$[2].name").value("Updated LV2"))
            .andExpect(jsonPath("$[2].level").value(2))
            .andExpect(jsonPath("$[3].name").value("Updated LV3"))
            .andExpect(jsonPath("$[3].level").value(3))
            .andExpect(jsonPath("$[4].name").value("Updated LV4"))
            .andExpect(jsonPath("$[4].level").value(4))
            .andExpect(jsonPath("$[5].name").value("Updated LV5"))
            .andExpect(jsonPath("$[5].level").value(5))
            .andExpect(jsonPath("$[6].name").value("LV6"))
            .andExpect(jsonPath("$[6].level").value(6))
        val board = boardRepository.findByName("Board 1")!!
        val user = userRepository.findByUsername("User 1")!!
        assertEquals("LV6", board.readableRole.name)
        assertEquals("LV6", user.role.name)
    }
}