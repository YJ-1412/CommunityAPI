package com.portfolio.community.entity

import jakarta.persistence.*

@Entity
data class AppConfig(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "config_key", nullable = false, unique = true)
    var key: String,

    @Column(name = "config_value")
    var value: String
)