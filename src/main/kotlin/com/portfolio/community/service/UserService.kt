package com.portfolio.community.service

import com.portfolio.community.dto.user.*
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.exception.NotFoundException
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val roleRepository: RoleRepository
): UserDetailsService {

    @Transactional
    fun createUser(userCreateRequest: UserCreateRequest): UserResponse {

        if(userRepository.existsByUsername(userCreateRequest.username!!)) throw IllegalArgumentException("User with username already exists")

        val defaultRole = roleRepository.findFirstByOrderByLevel() ?: throw IllegalArgumentException("No role exists")

        val user = UserEntity(
            username = userCreateRequest.username,
            password = passwordEncoder.encode(userCreateRequest.password),
            role = defaultRole
        )

        return UserResponse(userRepository.save(user))
    }

    @Transactional(readOnly = true)
    fun getUserById(userId: Long): UserResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        return UserResponse(user)
    }

    @Transactional(readOnly = true)
    fun getPrincipalById(userId: Long): Principal {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        return Principal(user)
    }

    @Transactional
    fun updateUser(userId: Long, userUpdateRequest: UserUpdateRequest): UserResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)

        val userFindByUsername = userRepository.findByUsername(userUpdateRequest.username!!)
        if(userFindByUsername != null && userFindByUsername.id != user.id) throw IllegalArgumentException("User with username already exists")

        user.update(
            username = userUpdateRequest.username,
            password = passwordEncoder.encode(userUpdateRequest.password)
        )
        user.invalidateRefreshToken()

        return UserResponse(user)
    }

    @Transactional
    fun changeRole(userId: Long, roleId: Long): UserResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        val role = roleRepository.findByIdOrNull(roleId) ?: throw NotFoundException("Role", "ID", roleId)

        if (user.role.id == role.id) throw IllegalStateException("User already has role")

        user.updateRole(role)
        user.invalidateRefreshToken()

        return UserResponse(user)
    }

    @Transactional
    fun setStaff(userId: Long): UserResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        if (user.isAdminOrStaff()) throw IllegalStateException("User is already admin or staff")

        user.setStaff()
        user.invalidateRefreshToken()

        return UserResponse(user)
    }

    @Transactional
    fun setRegular(userId: Long): UserResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        if (!user.isStaff) throw IllegalStateException("User is admin or already regular")

        user.setRegular()
        user.invalidateRefreshToken()

        return UserResponse(user)
    }

    @Transactional
    fun saveRefreshToken(userId: Long, refreshToken: String) {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        user.refreshToken = refreshToken
    }

    @Transactional(readOnly = true)
    fun getRefreshToken(userId: Long): String? {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        return user.refreshToken
    }

    @Transactional
    fun deleteUser(userId: Long) {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundException("User", "ID", userId)
        userRepository.delete(user)
    }

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByUsername(username) ?: throw BadCredentialsException("Bad credentials")
    }
}