package com.portfolio.community.controller

import com.portfolio.community.dto.role.RoleCreateRequest
import com.portfolio.community.dto.role.RoleResponse
import com.portfolio.community.dto.role.RoleUpdateRequest
import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.service.RoleService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RoleEntityControllerTest {

    private lateinit var roleController: RoleController
    private lateinit var roleService: RoleService

    @BeforeEach
    fun setUp() {
        roleService = mockk()
        roleController = RoleController(roleService)
    }

    @Test
    fun given_RoleExists_when_GetAllRoles_then_ReturnOkAndRoleList() {
        //Given
        val roles = (1..10).map { RoleResponse(RoleEntity(name = "User Lv$it", level = it - 1, id = it.toLong())) }
        every { roleService.getAllRoles() } returns roles

        //When
        val result = roleController.getAllRoles()

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(10, result.body!!.size)
        assertEquals("User Lv1", result.body!![0].name)
        assertEquals(0, result.body!![0].level)
    }

    @Test
    fun given_NoRoleExists_when_GetAllRoles_then_ReturnNoContent() {
        //Given
        every { roleService.getAllRoles() } returns listOf()

        //When
        val result = roleController.getAllRoles()

        //Then
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
    }

    @Test
    fun given_ValidData_when_CreateRole_then_ReturnCreatedAndNewRole() {
        //Given
        val roleCreateRequest = RoleCreateRequest(name = "User", level = 0)
        val newRole = RoleResponse(RoleEntity(name = "User", level = 0, id = 1))
        every { roleService.createRole(roleCreateRequest) } returns newRole

        //When
        val result = roleController.createRole(roleCreateRequest)

        //Then
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals("/roles/1", result.headers.location!!.path)
        assertEquals("User", result.body!!.name)
        assertEquals(0, result.body!!.level)
    }

    @Test
    fun given_ValidData_when_UpdateRole_then_ReturnOkAndUpdatedRole() {
        //Given
        val roleUpdateRequest = RoleUpdateRequest(name = "Updated", level = 0)
        val updatedRole = RoleResponse(RoleEntity(name = "Updated", level = 0, id = 1))
        every { roleService.updateRole(1, roleUpdateRequest) } returns updatedRole

        //When
        val result = roleController.updateRole(1, roleUpdateRequest)

        //Then
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("Updated", result.body!!.name)
        assertEquals(0, result.body!!.level)
    }

}