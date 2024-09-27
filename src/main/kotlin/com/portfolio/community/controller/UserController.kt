package com.portfolio.community.controller

import com.portfolio.community.dto.user.*
import com.portfolio.community.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/users/{userId}")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        val user = userService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("@securityService.canUpdateUser(authentication, #userId)")
    fun updateUser(@PathVariable userId: Long, @Valid @RequestBody userUpdateRequest: UserUpdateRequest) : ResponseEntity<UserResponse> {
        val updatedUser = userService.updateUser(userId, userUpdateRequest)
        return ResponseEntity.ok(updatedUser)
    }

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("@securityService.isAdminOrStaff(authentication)")
    fun changeRole(@PathVariable userId: Long, @RequestBody roleId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.changeRole(userId, roleId)
        return ResponseEntity.ok(updatedUser)
    }

    @PostMapping("/staff")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun setStaff(@RequestBody userId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.setStaff(userId)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/staff/{userId}")
    @PreAuthorize("@securityService.isAdmin(authentication)")
    fun setRegular(@PathVariable userId: Long) : ResponseEntity<UserResponse> {
        val updatedUser = userService.setRegular(userId)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("@securityService.canDeleteUser(authentication, #userId)")
    fun deleteUser(@PathVariable userId: Long) : ResponseEntity<Void> {
        userService.deleteUser(userId)
        return ResponseEntity.noContent().build()
    }

}