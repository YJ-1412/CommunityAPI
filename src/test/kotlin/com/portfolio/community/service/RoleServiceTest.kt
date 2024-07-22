package com.portfolio.community.service

import com.portfolio.community.dto.role.RoleCreateRequest
import com.portfolio.community.dto.role.RoleUpdateRequest
import com.portfolio.community.entity.Role
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.RoleRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull

class RoleServiceTest {
    private lateinit var roleService: RoleService
    private lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setUp() {
        roleRepository = mockk()
        roleService = RoleService(roleRepository)
    }

    @Test
    fun given_RoleExists_when_GetAllRoles_then_ReturnRoleList() {
        //Given
        val roles = (1..10).map { Role(name = "Level$it", level = it, id = it.toLong()) }
        every { roleRepository.findAllByOrderByLevel() } returns roles

        //When
        val result = roleService.getAllRoles()

        //Then
        assertEquals("Level1", result[0].name)
        assertEquals(1, result[0].level)
    }

    @Test
    fun given_ValidData_when_CreateRole_then_CreateAndReturnNewRole() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "New Role", level = 1)
        val newRole = Role(name = "New Role", level = 1)
        every { roleRepository.existsByName("New Role") } returns false
        every { roleRepository.existsByLevel(1) } returns false
        every { roleRepository.save(any()) } returns newRole

        //When
        val result = roleService.createRole(roleCreateRequest)

        //Then
        assertEquals("New Role", result.name)
        assertEquals(1, result.level)
        verify(exactly = 1) { roleRepository.save(any()) }
    }

    @Test
    fun given_DuplicateName_when_CreateRole_then_ThrowIllegalArgumentException() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "New Role", level = 1)
        every { roleRepository.existsByName("New Role") } returns true

        //When & Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            roleService.createRole(roleCreateRequest)
        }
        assertEquals("Role with name already exists", ex.message)
    }

    @Test
    fun given_DuplicateLevel_when_CreateRole_then_ThrowIllegalArgumentException() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "New Role", level = 1)
        every { roleRepository.existsByName("New Role") } returns false
        every { roleRepository.existsByLevel(1) } returns true

        //When & Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            roleService.createRole(roleCreateRequest)
        }
        assertEquals("Role with level already exists", ex.message)
    }

    @Test
    fun given_ValidData_when_UpdateRole_then_UpdateAndReturnUpdatedRole() {
        //Given
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 1)
        val oldRole = Role(name = "Old Role", level = 1, id = 1L)
        every { roleRepository.findByIdOrNull(1L) } returns oldRole
        every { roleRepository.findByName("Updated Role") } returns null
        every { roleRepository.findByLevel(1)} returns oldRole

        //When
        val result = roleService.updateRole(1L, roleUpdateRequest)

        //Then
        assertEquals("Updated Role", result.name)
        assertEquals(1, result.level)
    }

    @Test
    fun given_InvalidRoleId_when_UpdateRole_then_ThrowNotFoundException() {
        //Given
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 1)
        every { roleRepository.findByIdOrNull(1L) } returns null

        //When & Then
        val ex = assertThrows(NotFoundException::class.java) {
            roleService.updateRole(1L, roleUpdateRequest)
        }
        assertEquals("Role with ID 1 Not Found", ex.message)
    }

    @Test
    fun given_DuplicateName_when_UpdateRole_then_ThrowIllegalArgumentException() {
        //Given
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 1)
        val oldRole = Role(name = "Old Role", level = 1, id = 1L)
        val roleWithDuplicateName = Role(name = "Updated Role", level = 2, id = 2L)
        every { roleRepository.findByIdOrNull(1L) } returns oldRole
        every { roleRepository.findByName("Updated Role") } returns roleWithDuplicateName

        //When & Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            roleService.updateRole(1L, roleUpdateRequest)
        }
        assertEquals("Role with name already exists", ex.message)
    }

    @Test
    fun given_DuplicateLevel_when_UpdateRole_then_ThrowIllegalArgumentException() {
        //Given
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated Role", level = 2)
        val oldRole = Role(name = "Old Role", level = 1, id = 1L)
        val roleWithDuplicateLevel = Role(name = "Other Role", level = 2, id = 2L)
        every { roleRepository.findByIdOrNull(1L) } returns oldRole
        every { roleRepository.findByName("Updated Role") } returns null
        every { roleRepository.findByLevel(2) } returns roleWithDuplicateLevel

        //When & Then
        val ex = assertThrows(IllegalArgumentException::class.java) {
            roleService.updateRole(1L, roleUpdateRequest)
        }
        assertEquals("Role with level already exists", ex.message)
    }

}