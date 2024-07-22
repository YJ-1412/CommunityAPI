package com.portfolio.community.service

import com.portfolio.community.dto.role.RoleBatchUpdateRequest
import com.portfolio.community.dto.role.RoleCreateRequest
import com.portfolio.community.dto.role.RoleUpdateRequest
import com.portfolio.community.entity.BoardEntity
import com.portfolio.community.entity.Role
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.BoardRepository
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
class RoleServiceIntegrationTest {

    @Autowired private lateinit var roleService: RoleService

    @Autowired private lateinit var roleRepository: RoleRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var boardRepository: BoardRepository

    @BeforeEach
    fun setup() {
        boardRepository.deleteAll()
        userRepository.deleteAll()
        roleRepository.deleteAll()
    }

    @Test
    fun given_RoleExists_when_GetAllRoles_then_ReturnRoleList() {
        //Given
        val roles = (0..4).map { Role(name = "User LV$it", level = it) }
        roleRepository.saveAll(roles)

        //When
        val result = roleService.getAllRoles()

        //Then
        assertEquals(5, result.size)
        assertEquals("User LV0", result[0].name)
        assertEquals(0, result[0].level)
    }

    @Test
    fun given_ValidData_when_CreateRole_then_CreateAndReturnNewRole() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "New Role", level = 0)

        //When
        val result = roleService.createRole(roleCreateRequest)

        //Then
        assertEquals("New Role", result.name)
        assertEquals(0, result.level)
    }

    @Test
    fun given_DuplicatedName_when_CreateRole_then_ThrowIllegalArgumentException() {
        //Given
        roleRepository.save(Role(name = "New Role", level = 0))
        val roleCreateRequest = RoleCreateRequest(name = "New Role", level = 1)

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            roleService.createRole(roleCreateRequest)
        }
        assertEquals("Role with name already exists", ex.message)
    }

    @Test
    fun given_DuplicatedLevel_when_CreateRole_then_ThrowIllegalArgumentException() {
        //Given
        roleRepository.save(Role(name = "Test Role", level = 0))
        val roleCreateRequest = RoleCreateRequest(name = "New Role", level = 0)

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            roleService.createRole(roleCreateRequest)
        }
        assertEquals("Role with level already exists", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdateRole_then_UpdateAndReturnUpdatedRole() {
        //Given
        val testRole = roleRepository.save(Role(name = "Test Role", level = 0))
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 1)

        //When
        val result = roleService.updateRole(testRole.id, roleUpdateRequest)

        //Then
        assertEquals("Updated Role", result.name)
        assertEquals(1, result.level)
    }

    @Test
    fun given_InvalidRoleId_when_UpdateRole_then_ThrowNotFoundException() {
        //Given
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 1)

        //When & Then
        val ex = assertThrows<NotFoundException> {
            roleService.updateRole(-1, roleUpdateRequest)
        }
        assertEquals("Role with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_DuplicatedName_when_UpdateRole_then_ThrowIllegalArgumentException() {
        //Given
        val testRole = roleRepository.save(Role(name = "Test Role", level = 0))
        roleRepository.save(Role(name = "Updated Role", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 2)

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            roleService.updateRole(testRole.id, roleUpdateRequest)
        }
        assertEquals("Role with name already exists", ex.message)
    }

    @Test
    fun given_DuplicatedLevel_when_UpdateRole_then_ThrowIllegalArgumentException() {
        //Given
        val testRole = roleRepository.save(Role(name = "Test Role 1", level = 0))
        roleRepository.save(Role(name = "Test Role 2", level = 1))
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 1)

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            roleService.updateRole(testRole.id, roleUpdateRequest)
        }
        assertEquals("Role with level already exists", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteRole_then_DeleteRoleAndMoveToDefaultRole() {
        //Given
        val sourceRole = roleRepository.save(Role(name = "User LV0", level = 0))
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = sourceRole) })
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = sourceRole) })
        val defaultRole = roleRepository.save(Role(name = "User LV1", level = 1))

        //When
        roleService.deleteRole(sourceRole.id)

        //Then
        val boards = boardRepository.findByReadableRoleIdOrderByPriority(defaultRole.id)
        val users = userRepository.findByRoleIdOrderByUsername(defaultRole.id)
        assertEquals(5, boards.size)
        assertEquals(5, users.size)
    }

    @Test
    fun given_OnlyOneRoleExists_when_DeleteRole_then_ThrowIllegalStateException() {
        //Given
        val sourceRole = roleRepository.save(Role(name = "User LV0", level = 0))

        //When & Then
        val ex = assertThrows<IllegalStateException> {
            roleService.deleteRole(sourceRole.id)
        }
        assertEquals("At least one role must exists", ex.message)
    }

    @Test
    fun given_InvalidRoleId_when_DeleteRole_then_ThrowNotFoundException() {
        //Given
        roleRepository.save(Role(name = "User LV0", level = 0))
        roleRepository.save(Role(name = "User LV1", level = 1))

        //When & Then
        val ex = assertThrows<NotFoundException> {
            roleService.deleteRole(-1)
        }
        assertEquals("Role with ID -1 Not Found", ex.message)
    }

    @Test
    fun given_ValidData_when_DeleteRoleAndMoveBoardsAndUsers_then_DeleteRoleAndMoveToTargetRole() {
        //Given
        val sourceRole = roleRepository.save(Role(name = "User LV0", level = 0))
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = sourceRole) })
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = sourceRole) })
        val targetRole = roleRepository.save(Role(name = "User LV1", level = 1))

        //When
        val result = roleService.deleteRoleAndMoveBoardsAndUsers(sourceRole.id, targetRole.id)

        //Then
        assertEquals("User LV1", result.name)
        val boards = boardRepository.findByReadableRoleIdOrderByPriority(targetRole.id)
        val users = userRepository.findByRoleIdOrderByUsername(targetRole.id)
        assertEquals(5, boards.size)
        assertEquals(5, users.size)
    }

    @Test
    fun given_SourceRoleEqualsTargetRole_when_DeleteRoleAndMoveBoardsAndUsers_then_ThrowIllegalArgumentException() {
        //Given
        val sourceRole = roleRepository.save(Role(name = "User LV0", level = 0))

        //When & Then
        val ex = assertThrows<IllegalArgumentException> {
            roleService.deleteRoleAndMoveBoardsAndUsers(sourceRole.id, sourceRole.id)
        }
        assertEquals("Source role and target role must be different", ex.message)
    }

    @Test
    fun given_InvalidRoleId_when_DeleteRoleAndMoveBoardsAndUsers_then_ThrowNotFoundException() {
        //Given
        val role = roleRepository.save(Role(name = "User LV0", level = 0))

        //When & Then
        val ex1 = assertThrows<NotFoundException> {
            roleService.deleteRoleAndMoveBoardsAndUsers(-1, role.id)
        }
        val ex2 = assertThrows<NotFoundException> {
            roleService.deleteRoleAndMoveBoardsAndUsers(role.id, -1)
        }
        assertEquals("Role with ID -1 Not Found", ex1.message)
        assertEquals("Role with ID -1 Not Found", ex2.message)
    }

    @Test
    fun given_ValidData_when_BatchUpdateRole_then_ReturnRoleList() {
        //Given
        //level 0~6까지 존재. 0~4를 1씩 올린 뒤, 새로운 0을 생성, 그리고 5는 삭제하며 6에 병합
        val roles = roleRepository.saveAll((0..6).map { Role(name = "LV$it", level = it) })
        val batchUpdateRequest = RoleBatchUpdateRequest(
            updates = (0..4).map { Pair(roles[it].id, RoleUpdateRequest(name = "Updated LV${it+1}", level = it+1)) },
            creates = listOf(RoleCreateRequest(name = "New LV0", level = 0)),
            moves = listOf(Pair(roles[5].id, roles[6].id))
        )
        userRepository.saveAll((1..5).map { UserEntity(username = "User $it", password = "password", role = roles[5]) })
        boardRepository.saveAll((1..5).map { BoardEntity(name = "Board $it", priority = it-1, readableRole = roles[5]) })

        //When
        val result = roleService.batchUpdateRole(batchUpdateRequest)

        //Then
        assertEquals("New LV0", result[0].name)
        assertEquals(0, result[0].level)
        assertEquals("Updated LV1", result[1].name)
        assertEquals(1, result[1].level)
        assertEquals("Updated LV2", result[2].name)
        assertEquals(2, result[2].level)
        assertEquals("Updated LV3", result[3].name)
        assertEquals(3, result[3].level)
        assertEquals("Updated LV4", result[4].name)
        assertEquals(4, result[4].level)
        assertEquals("Updated LV5", result[5].name)
        assertEquals(5, result[5].level)
        assertEquals("LV6", result[6].name)
        assertEquals(6, result[6].level)
        val boards = boardRepository.findByReadableRoleIdOrderByPriority(result[6].id)
        val users = userRepository.findByRoleIdOrderByUsername(result[6].id)
        assertEquals(5, boards.size)
        assertEquals(5, users.size)
    }
}