package com.portfolio.community.repository

import com.portfolio.community.entity.AppConfig
import org.springframework.data.jpa.repository.JpaRepository

interface AppConfigRepository : JpaRepository<AppConfig, Long> {
    fun findByKey(key: String): AppConfig?
}