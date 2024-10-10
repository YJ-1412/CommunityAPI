package com.portfolio.community.configuration

import com.portfolio.community.entity.AppConfig
import com.portfolio.community.entity.RoleEntity
import com.portfolio.community.entity.UserEntity
import com.portfolio.community.repository.AppConfigRepository
import com.portfolio.community.repository.RoleRepository
import com.portfolio.community.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
    val appConfigRepository: AppConfigRepository,
    val passwordEncoder: PasswordEncoder
) {
    @Bean
    fun initData(): CommandLineRunner {
        return CommandLineRunner {

            var defaultRole = roleRepository.findFirstByOrderByLevel()
            if (defaultRole == null) {
                defaultRole = roleRepository.save(RoleEntity(name = "LV0", level = 0))
            }

            val adminId = appConfigRepository.findByKey("admin_id")
            if (adminId == null || userRepository.findByIdOrNull(adminId.value.toLong())==null) {
                val admin = userRepository.save(UserEntity("Admin", passwordEncoder.encode("00000000"), role = defaultRole).apply { setAdmin() })
                appConfigRepository.save(AppConfig(key = "admin_id", value = "${admin.id}"))
            }
        }
    }
}